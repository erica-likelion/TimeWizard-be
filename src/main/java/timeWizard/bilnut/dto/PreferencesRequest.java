package timeWizard.bilnut.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PreferencesRequest {

    @JsonProperty("preferred_days")
    private List<String> preferredDays;

    @JsonProperty("preferred_start_time")
    private String preferredStartTime;

    @JsonProperty("preferred_end_time")
    private String preferredEndTime;

    @JsonProperty("target_credits")
    private Integer targetCredits;

    @JsonProperty("required_courses")
    private List<Long> requiredCourses;

    @JsonProperty("excluded_courses")
    private List<Long> excludedCourses;
}

