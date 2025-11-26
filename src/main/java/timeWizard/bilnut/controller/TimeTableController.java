package timeWizard.bilnut.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import timeWizard.bilnut.dto.*;
import timeWizard.bilnut.security.CustomUserDetails;
import timeWizard.bilnut.service.TimeTableService;

import java.util.List;

@Tag(name = "시간표", description = "시간표 생성, 조회, 삭제 및 AI 시간표 생성 API")
@RestController
@RequiredArgsConstructor
public class TimeTableController {
    private final TimeTableService timeTableService;
    private final StringRedisTemplate redisTemplate;

    @Operation(summary = "시간표 삭제", description = "지정된 ID의 시간표를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "시간표 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "시간표를 찾을 수 없음")
    })
    @DeleteMapping("timetable/{timetableId}")
    public String deleteTimeTable(
            @Parameter(description = "삭제할 시간표 ID") @PathVariable String timetableId) {
        timeTableService.deleteTimeTable(timetableId);
        return "delete timetable success";
    }

    @Operation(summary = "AI 시간표 생성 요청", description = "AI를 사용하여 시간표를 생성하도록 요청합니다. 반환받은 UUID 키를 상태 확인 API 경로에 활용해야 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "요청 접수됨 - UUID 키 반환"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    @PostMapping("/ai/generate-timetable")
    public ResponseEntity<String> requestAiTimetable(@RequestBody AiTimetableRequestData aiTimetableRequestData, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String uuidKey =  timeTableService.requestAiTimeTable(aiTimetableRequestData, customUserDetails.getUserId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(uuidKey);
    }

    @Operation(summary = "시간표 저장", description = "생성된 시간표를 저장합니다. 이름 저장하며 시간표의 수업들도 함께 저장합니다. 시간표 AI 생성시에는 DB에 저장하지 않고 이 API로 저장합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "시간표 저장 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PostMapping("/save-timetable")
    public ResponseEntity<Void> saveTimetable(
            @RequestBody TimetableSaveRequestData timetableSaveRequestData,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        timeTableService.saveTimetable(timetableSaveRequestData, userDetails.getUserId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "AI 시간표 생성 상태 확인", description = "UUID 키로 AI 시간표 생성 작업의 진행 상태를 확인합니다. 완성시에는 AI가 만든 시간표 JSON을 data에 전달합니다. AI 응답이 완료되지 않아도 200 상태 코드로 가기는 하지만 data 필드는 null로 전달됩니다. 응답 상태 필드가 WAITING 아니면 ERROR 로 처리됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상태 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 키로 요청된 작업을 찾을 수 없음")
    })

    @GetMapping("/check/{uuidKey}/status")
    public ResponseEntity<AiStatusResponse> checkAiResponse(
            @Parameter(description = "AI 시간표 생성 요청 시 받은 UUID 키") @PathVariable String uuidKey) {
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


    @Operation(summary = "시간표 목록 조회", description = "현재 사용자의 모든 시간표 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "시간표 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/timetable/lists")
    public ResponseEntity<List<TimetableListData>> getTimetableIds(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(timeTableService.getTimetableList(userDetails.getUserId()));
    }

    @Operation(summary = "시간표의 강의 목록 조회", description = "특정 시간표에 포함된 모든 강의 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "강의 목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "시간표를 찾을 수 없음")
    })
    @GetMapping("/timetable/{id}/courses")
    public ResponseEntity<List<CourseResponseDTO>> getCourses(
            @Parameter(description = "조회할 시간표 ID") @PathVariable("id") String timetableId) {
        List<CourseResponseDTO> courses = timeTableService.getTimetableCourses(timetableId);
        return ResponseEntity.ok(courses);
    }

    @PostMapping("/timetable/{id}/plans")
    public ResponseEntity<String> getPlans(@PathVariable("id") String timetableId) {
        String uuidKey = timeTableService.makePlanner(timetableId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(uuidKey);
    }
}
