package timeWizard.bilnut.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import timeWizard.bilnut.entity.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
