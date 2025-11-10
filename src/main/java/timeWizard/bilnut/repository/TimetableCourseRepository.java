package timeWizard.bilnut.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import timeWizard.bilnut.entity.TimetableCourse;

import java.util.List;

public interface TimetableCourseRepository extends JpaRepository<TimetableCourse, Long> {

    @Query("SELECT tc FROM TimetableCourse tc " +
           "JOIN FETCH tc.timetable t " +
           "JOIN FETCH tc.course c " +
           "LEFT JOIN FETCH c.courseTimes " +
           "WHERE tc.timetable.id = :timetableId")
    List<TimetableCourse> findByTimetableIdWithCourseAndTimes(@Param("timetableId") String timetableId);
}
