package timeWizard.bilnut.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CourseTimeRequestDTO {
    private String day;

    @JsonProperty("start_time")
    private Integer startTime;

    @JsonProperty("end_time")
    private Integer endTime;
}
