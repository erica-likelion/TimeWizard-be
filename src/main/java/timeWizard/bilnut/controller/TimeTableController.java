package timeWizard.bilnut.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping
    public String requestAiTimetable(AiTimetableRequestData aiTimetableRequestData) {
        timeTableService.requestAiTimeTable(aiTimetableRequestData);
    }
}
