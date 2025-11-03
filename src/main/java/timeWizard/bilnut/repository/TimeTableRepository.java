package timeWizard.bilnut.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import timeWizard.bilnut.entity.Timetable;

public interface TimeTableRepository extends JpaRepository<Timetable, Long> {
    @Modifying
    @Query("DELETE FROM TimeTable t WHERE t.id = :id")
    long deleteByIdCustom(@Param("id") Long id);
}
