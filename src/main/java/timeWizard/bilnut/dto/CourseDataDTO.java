package timeWizard.bilnut.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CourseDataDTO {
    @JsonProperty("course_id")
    private Long courseId;

    @JsonProperty("course_name")
    private String courseName;

    private String professor;

    private Integer credit;

    @JsonProperty("course_type")
    private String courseType;

    @JsonProperty("course_times")
    private List<CourseTimeRequestDTO> courseTimes;
}
