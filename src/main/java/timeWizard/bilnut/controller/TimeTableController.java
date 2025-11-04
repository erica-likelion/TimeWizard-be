package timeWizard.bilnut.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import timeWizard.bilnut.dto.AiTimetableRequestData;
import timeWizard.bilnut.service.TimeTableService;

@Controller
@RequiredArgsConstructor
public class TimeTableController {
    private final TimeTableService timeTableService;
    private final StringRedisTemplate redisTemplate;

    @DeleteMapping("timetable/{timetableId}")
    public String deleteTimeTable(@PathVariable Long timetableId) {
        timeTableService.deleteTimeTable(timetableId);
        return "delete timetable success";
    }

    @PostMapping("/ai/generate-timetable")
    public String requestAiTimetable(AiTimetableRequestData aiTimetableRequestData) {
        return timeTableService.requestAiTimeTable(aiTimetableRequestData, 1L); // 로그인 구현안돼서 더미 데이터
    }

    @PostMapping("/save-timetable/{uuidKey}")
    public ResponseEntity<Void> saveTimetable(@PathVariable String uuidKey, @RequestParam("name") String name) {
        timeTableService.saveTimetable(uuidKey, name);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check/{uuidKey}/status")
    public ResponseEntity<String> checkAiResponse(@PathVariable String uuidKey) {
        String val = redisTemplate.opsForValue().get(uuidKey);

        if (val.equals("WAITING")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Still Waiting");
        } else if (val.equals("ERROR")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ai Request Error");
        } else {
            return ResponseEntity.ok().body(val);
        }
    }
}
