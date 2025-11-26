package timeWizard.bilnut.service;

import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import timeWizard.bilnut.config.exception.EntityNotFound;
import timeWizard.bilnut.dto.*;
import timeWizard.bilnut.entity.Course;
import timeWizard.bilnut.entity.CourseTimes;
import timeWizard.bilnut.entity.Timetable;
import timeWizard.bilnut.entity.TimetableCourse;
import timeWizard.bilnut.entity.User;
import timeWizard.bilnut.repository.CourseRepository;
import timeWizard.bilnut.repository.TimeTableRepository;
import timeWizard.bilnut.repository.TimetableCourseRepository;
import timeWizard.bilnut.repository.UserRepository;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeTableService {
    private final TimeTableRepository timeTableRepository;
    private final WebClient webClient;
    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final TimetableCourseRepository timetableCourseRepository;

    public String requestAiTimeTable(AiTimetableRequestData aiTimetableRequestData, Long userId) {
        String redisKey = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set(redisKey, "WAITING", Duration.ofMinutes(10));
        sendAiTimetableRequest(aiTimetableRequestData.requestText(),
                aiTimetableRequestData.maxCredit(),
                aiTimetableRequestData.targetCredit(),
                redisKey,
                userId);
        return redisKey;
    }

    @Async("aiApiRequestExecutor")
    @Transactional
    public void sendAiTimetableRequest(String requestText, Integer maxCredit, Integer targetCredit, String redisKey, Long userId) {
        AnalyzeRequirementsRequestDTO analyzeRequest = new AnalyzeRequirementsRequestDTO(requestText);
        User user = userRepository.findById(userId).orElseThrow(EntityExistsException::new);

        webClient.post()
                .uri("/analyze-requirements")
                .bodyValue(analyzeRequest)
                .retrieve()
                .bodyToMono(AnalyzeRequirementsResponse.class)
                .flatMap(analyzeResponse -> {
                    log.info(analyzeResponse.toString());
                    List<CourseDataDTO> filteredCourses = filterCourses(analyzeResponse, user);

                    TimetableRequestDTO timetableRequest = TimetableRequestDTO.builder()
                            .depart(user.getMajor())
                            .grade(user.getGrade())
                            .semester(2)
                            .goalCredit(targetCredit)
                            .maxCredit(maxCredit)
                            .requirement(requestText)
                            .courses(filteredCourses)
                            .build();

                    return webClient.post()
                            .uri("/generate-timetable")
                            .bodyValue(timetableRequest)
                            .retrieve()
                            .bodyToMono(String.class);
                })
                .doOnSuccess(jsonResponse ->
                        redisTemplate.opsForValue().set(redisKey, jsonResponse, Duration.ofMinutes(10)))
                .doOnError(error -> {
                    log.error("Error during AI request", error);
                    redisTemplate.opsForValue().set(redisKey, "ERROR", Duration.ofMinutes(10));
                })
                .subscribe();
    }

    public String makePlanner(String timetableId) {
        List<CourseResponseDTO> courseData = getTimetableCourses(timetableId);
        String redisKey = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set(redisKey, "WAITING", Duration.ofMinutes(10));
        sendAiPlannerRequest(redisKey, courseData);

        return redisKey;
    }

    @Async("aiApiRequestExecutor")
    public void sendAiPlannerRequest(String redisKey, List<CourseResponseDTO> courseData) {
        webClient.post()
                .uri("/prioritize-courses")
                .bodyValue(courseData)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(jsonResponse ->
                        redisTemplate.opsForValue().set(redisKey, jsonResponse, Duration.ofMinutes(10)))
                .doOnError(error -> {
                    log.error("Error during AI request", error);
                    redisTemplate.opsForValue().set(redisKey, "ERROR", Duration.ofMinutes(10));
                })
                .subscribe();
    }

    private List<CourseDataDTO> filterCourses(AnalyzeRequirementsResponse analyzeResponse, User user) {
        List<Course> allCourses = courseRepository.findByDepart(user.getMajor(), analyzeResponse.getIncludeCourses());

        return allCourses.stream()
                .filter(course -> {
                    if (analyzeResponse.getExcludeCourses() != null && !analyzeResponse.getExcludeCourses().isEmpty()) {
                        boolean excluded = analyzeResponse.getExcludeCourses().stream()
                                .anyMatch(exclude ->
                                        course.getCourseName().contains(exclude));
                        if (excluded) return false;
                    }

                    if (analyzeResponse.getExcludeTimeBlocks() != null && !analyzeResponse.getExcludeTimeBlocks().isEmpty()) {
                        boolean hasConflict = course.getCourseTimes().stream()
                                .anyMatch(courseTime ->
                                        analyzeResponse.getExcludeTimeBlocks().stream()
                                                .anyMatch(excludeBlock -> isTimeConflict(courseTime, excludeBlock)));
                        if (hasConflict) return false;
                    }

                    return true;
                })
                .map(this::convertToCourseDataDTO)
                .toList();
    }

    private boolean isTimeConflict(CourseTimes courseTime, ExcludeTimeBlockDTO excludeBlock) {
        // "all"인 경우 모든 요일에 적용
        if ("all".equalsIgnoreCase(excludeBlock.getDay())) {
            return isTimeOverlap(courseTime.getStartTime(), courseTime.getEndTime(),
                    excludeBlock.getStartTime(), excludeBlock.getEndTime());
        }

        // 요일이 일치하고 시간이 겹치는지 확인
        if (courseTime.getDayOfWeek().name().equals(excludeBlock.getDay())) {
            return isTimeOverlap(courseTime.getStartTime(), courseTime.getEndTime(),
                    excludeBlock.getStartTime(), excludeBlock.getEndTime());
        }

        return false;
    }

    private boolean isTimeOverlap(Integer start1, Integer end1, Integer start2, Integer end2) {
        // 시간이 겹치는지 확인
        return start1 < end2 && start2 < end1;
    }

    private CourseDataDTO convertToCourseDataDTO(Course course) {
        List<CourseTimeRequestDTO> courseTimes = course.getCourseTimes().stream()
                .map(ct -> CourseTimeRequestDTO.builder()
                        .day(ct.getDayOfWeek().name())
                        .startTime(ct.getStartTime())
                        .endTime(ct.getEndTime())
                        .build())
                .toList();

        return CourseDataDTO.builder()
                .courseId(course.getCourseId())
                .courseName(course.getCourseName())
                .professor(course.getProfessor())
                .credit(course.getCredits())
                .courseType(course.getCourseType())
                .courseTimes(courseTimes)
                .build();
    }

    @Transactional
    public void saveTimetable(TimetableSaveRequestData timetableSaveRequestData, Long userId) {
        User user = userRepository.getReferenceById(userId);

        Timetable timetable = Timetable.builder()
                .id(timetableSaveRequestData.getUuidKey())
                .aiComment(timetableSaveRequestData.getAiComment())
                .user(user)
                .timetableName(timetableSaveRequestData.getName())
                .build();

        List<Long> courseIds = timetableSaveRequestData.getCourseIds();
        var courses = courseRepository.findAllById(courseIds);

        var courseMap = courses.stream()
                .collect(java.util.stream.Collectors.toMap(
                        course -> course.getCourseId(),
                        course -> course
                ));

        List<Long> missingCourseIds = courseIds.stream()
                .filter(id -> !courseMap.containsKey(id))
                .toList();

        if (!missingCourseIds.isEmpty()) {
            log.error("Missing courseIds in DB: {}", missingCourseIds);
            throw new IllegalArgumentException("Following course IDs do not exist: " + missingCourseIds);
        }

        List<TimetableCourse> timetableCourseList = courseIds.stream()
                .map(id -> {
                    var course = courseMap.get(id);
                    return TimetableCourse.builder()
                            .course(course)
                            .timetable(timetable)
                            .build();
                })
                .toList();

        timetable.getTimetableCourses().addAll(timetableCourseList);

        timeTableRepository.save(timetable);
    }



    @Transactional
    public void deleteTimeTable(String timetableId) {
        Timetable timetable = timeTableRepository.findById(timetableId)
                .orElseThrow(() -> new EntityNotFound("timetable not found with given id"));

        timeTableRepository.delete(timetable);
    }

    public List<TimetableListData> getTimetableList(Long userId) {
        return timeTableRepository.getTimetableList(userId);
    }


    public List<CourseResponseDTO> getTimetableCourses(String timetableId) {
        List<TimetableCourse> timetableCourses = timetableCourseRepository
                .findByTimetableIdWithCourseAndTimes(timetableId);

        return timetableCourses.stream()
                .map(tc -> CourseResponseDTO.from(tc.getCourse()))
                .toList();
    }
}
