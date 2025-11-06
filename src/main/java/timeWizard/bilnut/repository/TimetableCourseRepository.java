package timeWizard.bilnut.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import timeWizard.bilnut.entity.TimetableCourse;

public interface TimetableCourseRepository extends JpaRepository<TimetableCourse, Long> {
}
