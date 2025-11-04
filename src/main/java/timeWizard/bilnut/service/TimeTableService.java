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
import timeWizard.bilnut.config.exception.EntityNotFound;
import timeWizard.bilnut.config.exception.NoDeletedRowException;
import timeWizard.bilnut.dto.AiRequestFormData;
import timeWizard.bilnut.dto.AiTimetableRequestData;
import timeWizard.bilnut.dto.AiTimetableResponse;
import timeWizard.bilnut.entity.Timetable;
import timeWizard.bilnut.entity.User;
import timeWizard.bilnut.repository.TimeTableRepository;
import timeWizard.bilnut.repository.UserRepository;

import java.time.Duration;
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

    public String requestAiTimeTable(AiTimetableRequestData aiTimetableRequestData, Long userId) {
        String redisKey = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set(redisKey, "WAITING", Duration.ofMinutes(10));
        sendAiRequest(aiTimetableRequestData.requestText(),
                aiTimetableRequestData.maxCredit(),
                aiTimetableRequestData.targetCredit(),
                redisKey, userId);
        return redisKey;
    }

    @Async("aiApiRequestExecutor")
    @Transactional
    public void sendAiRequest(String requestText, Integer maxCredit, Integer targetCredit, String redisKey, Long userId) { // 아직 로그인이 구현이 안돼서 더미 데이터로 구현
        AiRequestFormData aiRequestFormData = new AiRequestFormData("로봇공학과",
                2, 1, targetCredit, maxCredit, requestText,
                "https://nuc-opencloud.pdj.kr/data/likelion/time_wizard/demo_data/erica_sugang_1001.csv",
                "https://site.hanyang.ac.kr/documents/11050741/13154841/이수체계도(로봇공학과).png?t=1684909574755");

        Mono<AiTimetableResponse> timetableResponseMono = webClient.post()
                .uri("/generate-timetable")
                .bodyValue(aiRequestFormData)
                .retrieve()
                .bodyToMono(AiTimetableResponse.class)
                .doOnSuccess(response -> {
                    try {
                        String jsonResponse = objectMapper.writeValueAsString(response);
                        redisTemplate.opsForValue().set(redisKey, jsonResponse, Duration.ofMinutes(10));

                    } catch (JsonProcessingException e) {
                        log.error("Redis 응답 저장 실패: JSON 직렬화 오류", e);
                    }})
                .doOnError(error ->
                        redisTemplate.opsForValue().set(redisKey, "ERROR", Duration.ofMinutes(10)));
    }

//    private void saveTimetable(AiTimetableResponse aiTimetableResponse, Long userId) {
//        User user = userRepository.getReferenceById(userId);
//
//        Timetable timetable = Timetable.builder()
//                .aiComment(aiTimetableResponse.getAiComment())
//                .user(user).build();
//
//
//        timeTableRepository.save(timetable);
//    }


    @Transactional
    public void saveTimetable(String redisKey, String timetableName) {
        Timetable timetable = timeTableRepository.findByRedisKey(redisKey)
                .orElseThrow(() -> new EntityNotFound("timetable not found by given key"));

        timetable.setTimetableName(timetableName);
    }

    @Transactional
    public void deleteTimeTable(Long timetableId) {
        int deletedRows = timeTableRepository.deleteByIdCustom(timetableId);

        if (deletedRows == 0) {
            throw new NoDeletedRowException("No deleted row found");
        }
    }
}
