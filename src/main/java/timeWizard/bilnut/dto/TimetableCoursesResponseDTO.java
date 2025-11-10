package timeWizard.bilnut.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class TimetableCoursesResponseDTO {
    private String timetableId;
    private String timetableName;
    private List<CourseResponseDTO> courses;
}
