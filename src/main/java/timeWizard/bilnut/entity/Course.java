package timeWizard.bilnut.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import timeWizard.bilnut.enums.DayOfWeek;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long courseId;

    @Column( length = 50, nullable = false)
    private String courseCode;

    @Column(length = 200, nullable = false)
    private String courseName;

    private String courseEnglishName;

    private Integer courseNumber;

    @Column(length = 100)
    private String professor;

    @Column(length = 100)
    private String major; // 교양이면 'ERICA 대학'

    private Integer section; // 분반

    private Integer grade; // 대상 학년 (NULL 가능)

    @Column(nullable = false)
    private Integer credits;

    private Integer lectureHours;

    private Integer practiceHours;

    @Column(nullable = false)
    private String courseType; // 이수 구분 '교양 선택', '전공 핵심'

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    private Integer startTime;

    private Integer endTime;

    @Column(length = 100)
    private String classroom;

    private Integer capacity;

    @Column(length = 10)
    private String semester;
}