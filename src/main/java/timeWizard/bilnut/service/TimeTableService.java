package timeWizard.bilnut.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import timeWizard.bilnut.config.exception.EntityNotFound;
import timeWizard.bilnut.config.exception.NoDeletedRowException;
import timeWizard.bilnut.dto.*;
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

    public String requestAiTimeTable(AiTimetableRequestData aiTimetableRequestData) {
        String redisKey = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set(redisKey, "WAITING", Duration.ofMinutes(10));
        sendAiRequest(aiTimetableRequestData.requestText(),
                aiTimetableRequestData.maxCredit(),
                aiTimetableRequestData.targetCredit(),
                redisKey);
        return redisKey;
    }

    @Async("aiApiRequestExecutor")
    @Transactional
    public void sendAiRequest(String requestText, Integer maxCredit, Integer targetCredit, String redisKey) { // 아직 로그인이 구현이 안돼서 더미 데이터로 구현
        AiRequestFormData aiRequestFormData = new AiRequestFormData("로봇공학과",
                2, 1, targetCredit, maxCredit, requestText,
                "https://storage.googleapis.com/mysmbuckettt/erica_courses_filtered.csv",
                "https://site.hanyang.ac.kr/documents/11050741/13154841/이수체계도(로봇공학과).png?t=1684909574755");

        webClient.post()
                .uri("/generate-timetable")
                .bodyValue(aiRequestFormData)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(jsonResponse ->
                        redisTemplate.opsForValue().set(redisKey, jsonResponse, Duration.ofMinutes(10)))
                .doOnError(error ->
                        redisTemplate.opsForValue().set(redisKey, "ERROR", Duration.ofMinutes(10)))
                .subscribe();
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
