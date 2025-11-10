package timeWizard.bilnut.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiRequestFormData(
        String depart,
        Integer grade,
        Integer semester,
        @JsonProperty("goal_credit") Integer goalCredit,
        @JsonProperty("max_credit") Integer maxCredit,
        String requirement,
        @JsonProperty("curriculum_csv_url") String curriculumCsvUrl,
        @JsonProperty("curriculum_image_url") String curriculumImageUrl
) {}
