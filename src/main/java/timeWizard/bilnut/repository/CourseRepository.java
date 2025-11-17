package timeWizard.bilnut.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import timeWizard.bilnut.entity.Course;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("SELECT DISTINCT c FROM Course c LEFT JOIN FETCH c.courseTimes WHERE c.major = :depart OR c.major = 'ERICA 대학' OR c.courseName IN :includeCourses")
    List<Course> findByDepart(@Param("depart") String depart, List<String> includeCourses);
}
