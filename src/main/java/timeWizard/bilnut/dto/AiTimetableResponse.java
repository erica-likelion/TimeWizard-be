package timeWizard.bilnut.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AiTimetableResponse {

    private List<Course> courses;

    @JsonProperty("ai_comment")
    private String aiComment;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Course {
        @JsonProperty("course_id")
        private String courseId;

        @JsonProperty("course_name")
        private String courseName;

        private String professor;

        @JsonProperty("day_of_week")
        private String dayOfWeek;  // mon, tue, wed, thu, fri, sat, sun

        @JsonProperty("start_time")
        private Integer startTime;

        @JsonProperty("end_time")
        private Integer endTime;
    }
}
