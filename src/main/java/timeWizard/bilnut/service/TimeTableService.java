package timeWizard.bilnut.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final ObjectMapper objectMapper;
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
                "https://nuc-opencloud.pdj.kr/data/likelion/time_wizard/demo_data/erica_sugang_1001.csv",
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

        List<TimetableCourse> timetableCourseList = timetableSaveRequestData.getCourseIds()
                        .stream().map(id ->
                        TimetableCourse.builder()
                                .course(courseRepository.getReferenceById(id))
                                .timetable(timetable).build()).toList();

        timetable.getTimetableCourses().addAll(timetableCourseList);

        timeTableRepository.save(timetable);
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
}
