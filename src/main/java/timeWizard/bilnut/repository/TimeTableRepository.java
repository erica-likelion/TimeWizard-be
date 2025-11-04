package timeWizard.bilnut.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import timeWizard.bilnut.entity.Timetable;

import java.util.Optional;

public interface TimeTableRepository extends JpaRepository<Timetable, Long> {
    @Modifying
    @Query("DELETE FROM Timetable t WHERE t.id = :id")
    int deleteByIdCustom(@Param("id") Long id);

    Optional<Timetable> findByRedisKey(String redisKey);
}
