package timeWizard.bilnut.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import timeWizard.bilnut.entity.Course;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class CourseResponseDTO {
    private Long courseId;
    private String courseCode;
    private String courseName;
    private String courseEnglishName;
    private Integer courseNumber;
    private String professor;
    private String major;
    private Integer section;
    private Integer grade;
    private Integer credits;
    private Integer lectureHours;
    private Integer practiceHours;
    private String courseType;
    private Integer capacity;
    private String semester;
    private List<CourseTimeDTO> courseTimes;

    public static CourseResponseDTO from(Course course) {
        List<CourseTimeDTO> courseTimeDTOs = course.getCourseTimes().stream()
                .map(ct -> CourseTimeDTO.builder()
                        .dayOfWeek(ct.getDayOfWeek())
                        .startTime(ct.getStartTime())
                        .endTime(ct.getEndTime())
                        .classroom(ct.getClassroom())
                        .build())
                .toList();

        return CourseResponseDTO.builder()
                .courseId(course.getCourseId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .courseEnglishName(course.getCourseEnglishName())
                .courseNumber(course.getCourseNumber())
                .professor(course.getProfessor())
                .major(course.getMajor())
                .section(course.getSection())
                .grade(course.getGrade())
                .credits(course.getCredits())
                .lectureHours(course.getLectureHours())
                .practiceHours(course.getPracticeHours())
                .courseType(course.getCourseType())
                .capacity(course.getCapacity())
                .semester(course.getSemester())
                .courseTimes(courseTimeDTOs)
                .build();
    }
}
