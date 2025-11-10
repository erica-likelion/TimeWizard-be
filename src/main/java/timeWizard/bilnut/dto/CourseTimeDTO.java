package timeWizard.bilnut.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import timeWizard.bilnut.enums.DayOfWeek;

@Getter
@AllArgsConstructor
@Builder
public class CourseTimeDTO {
    private DayOfWeek dayOfWeek;
    private Integer startTime;
    private Integer endTime;
    private String classroom;
}
