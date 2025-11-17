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
public class TimetableRequestDTO {
    private String depart;

    private Integer grade;

    private Integer semester;

    @JsonProperty("goal_credit")
    private Integer goalCredit;

    @JsonProperty("max_credit")
    private Integer maxCredit;

    private String requirement;

    private List<CourseDataDTO> courses;
}
