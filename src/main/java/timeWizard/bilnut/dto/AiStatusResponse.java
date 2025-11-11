package timeWizard.bilnut.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 시간표 생성 상태 응답")
public record AiStatusResponse(
        @Schema(description = "상태 코드", example = "WAITING ,COMPLETE, ERROR, NOT_FOUND")
        String status,

        @Schema(description = "상태 메시지", example = "시간표 생성 완료")
        String message,

        @Schema(description = "생성된 시간표 데이터 (COMPLETE 상태일 때만 포함, 아닐때는 null)",
                example = """
                        {
                          "courses": [
                            {
                              "course_id": "359",
                              "course_name": "학술영어2:글쓰기",
                              "professor": "Douglas James Scott",
                              "day_of_week": "fri",
                              "start_time": 780,
                              "end_time": 900
                            },
                            {
                              "course_id": "142",
                              "course_name": "AI+X:인공지능",
                              "professor": "김철수",
                              "day_of_week": "mon",
                              "start_time": 540,
                              "end_time": 660
                            }
                          ],
                          "ai_comment": "총 3개의 강의로 시간표를 구성했습니다. 금요일 오후에 영어 수업이 배치되었고, 월요일과 수요일에 AI 관련 전공 수업이 고르게 분포되어 있습니다."
                        }
                """)
        Object data
) {
}
