package timeWizard.bilnut.dto;

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
public class ExcludeTimeBlockDTO {
    private String day;
    private Integer startTime;
    private Integer endTime;
}
