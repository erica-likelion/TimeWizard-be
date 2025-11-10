package timeWizard.bilnut.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import timeWizard.bilnut.dto.TimetableListData;
import timeWizard.bilnut.entity.Timetable;
import java.util.List;

public interface TimeTableRepository extends JpaRepository<Timetable, String> {
    @Modifying
    @Query("DELETE FROM Timetable t WHERE t.id = :id")
    int deleteByIdCustom(@Param("id") String id);

    @Query("SELECT new timeWizard.bilnut.dto.TimetableListData(t.id, t.timetableName) FROM Timetable t WHERE t.user.id = :userId")
    List<TimetableListData> getTimetableList(Long userId);
}
