package timeWizard.bilnut.dto;

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
public class AnalyzeRequirementsResponse {
    private List<ExcludeTimeBlockDTO> excludeTimeBlocks;
    private List<String> includeCourses;
    private List<String> excludeCourses;


}
