package timeWizard.bilnut.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import timeWizard.bilnut.dto.AiStatusResponse;
import timeWizard.bilnut.dto.AiTimetableRequestData;
import timeWizard.bilnut.dto.AiTimetableResponse;
import timeWizard.bilnut.dto.TimetableSaveRequestData;
import timeWizard.bilnut.service.TimeTableService;

@RestController
@RequiredArgsConstructor
public class TimeTableController {
    private final TimeTableService timeTableService;
    private final StringRedisTemplate redisTemplate;

    @DeleteMapping("timetable/{timetableId}")
    public String deleteTimeTable(@PathVariable String timetableId) {
        timeTableService.deleteTimeTable(timetableId);
        return "delete timetable success";
    }

    @PostMapping("/ai/generate-timetable")
    public ResponseEntity<String> requestAiTimetable(@RequestBody AiTimetableRequestData aiTimetableRequestData) {
        String uuidKey =  timeTableService.requestAiTimeTable(aiTimetableRequestData, 1L); // 로그인 구현안돼서 더미 데이터
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(uuidKey);
    }

    @PostMapping("/save-timetable")
    public ResponseEntity<Void> saveTimetable(TimetableSaveRequestData timetableSaveRequestData) {
        timeTableService.saveTimetable(timetableSaveRequestData, 1L);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check/{uuidKey}/status")
    public ResponseEntity<AiStatusResponse> checkAiResponse(@PathVariable String uuidKey) {
        String val = redisTemplate.opsForValue().get(uuidKey);

        if (val == null) {
            AiStatusResponse response = new AiStatusResponse("NOT_FOUND", "해당 key로 요청된 작업을 찾을 수 없습니다.", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        if (val.equals("WAITING")) {
            AiStatusResponse response = new AiStatusResponse("WAITING", "AI가 시간표를 생성 중", null);
            return ResponseEntity.ok(response);
        } else if (val.equals("ERROR")) {
            AiStatusResponse response = new AiStatusResponse("ERROR", "AI 요청 중 오류가 발생", null);
            return ResponseEntity.ok(response);
        } else {
            AiStatusResponse response = new AiStatusResponse("COMPLETE", "시간표 생성 완료", val);
            return ResponseEntity.ok(response);
        }
    }
}
