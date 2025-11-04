package timeWizard.bilnut.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import timeWizard.bilnut.dto.AiTimetableRequestData;
import timeWizard.bilnut.service.TimeTableService;

@Controller
@RequiredArgsConstructor
public class TimeTableController {
    private final TimeTableService timeTableService;

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
}
