package timeWizard.bilnut.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "timetable_courses",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_timetable_course",
                        columnNames = {"timetable_id", "course_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class TimetableCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long timetableCourseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timetable_id", nullable = false)
    private Timetable timetable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @CreatedDate
    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
