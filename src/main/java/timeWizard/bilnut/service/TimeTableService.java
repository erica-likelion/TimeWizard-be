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
        log.info("=== saveTimetable 시작 ===");
        log.info("userId: {}", userId);
        log.info("uuidKey: {}", timetableSaveRequestData.getUuidKey());

        User user = userRepository.getReferenceById(userId);
        log.info("User: {}", user);

        Timetable timetable = Timetable.builder()
                .id(timetableSaveRequestData.getUuidKey())
                .aiComment(timetableSaveRequestData.getAiComment())
                .user(user)
                .timetableName(timetableSaveRequestData.getName())
                .build();

        List<Long> courseIds = timetableSaveRequestData.getCourseIds();
        log.info("요청받은 courseIds: {}", courseIds);

        // courseIds가 실제로 DB에 존재하는지 확인하고 조회
        var courses = courseRepository.findAllById(courseIds);
        log.info("findAllById 결과 개수: {}", courses.size());

        var courseMap = courses.stream()
                .collect(java.util.stream.Collectors.toMap(
                        course -> course.getCourseId(),
                        course -> course
                ));

        log.info("courseMap 크기: {}, keys: {}", courseMap.size(), courseMap.keySet());

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
                    log.info("courseId {} -> Course: {}", id, course);
                    if (course == null) {
                        log.error("courseMap.get({}) returned null!", id);
                    }
                    return TimetableCourse.builder()
                            .course(course)
                            .timetable(timetable)
                            .build();
                })
                .toList();

        log.info("TimetableCourse 개수: {}", timetableCourseList.size());

        timetable.getTimetableCourses().addAll(timetableCourseList);

        log.info("save 호출 직전");
        timeTableRepository.save(timetable);
        log.info("=== saveTimetable 완료 ===");
    }



    @Transactional
    public void deleteTimeTable(String timetableId) {
        int deletedRows = timeTableRepository.deleteByIdCustom(timetableId);

        if (deletedRows == 0) {
            throw new NoDeletedRowException("No deleted row found");
        }
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
