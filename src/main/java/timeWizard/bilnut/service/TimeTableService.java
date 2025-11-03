package timeWizard.bilnut.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import timeWizard.bilnut.config.exception.NoDeletedRowException;
import timeWizard.bilnut.repository.TimeTableRepository;

@Service
@RequiredArgsConstructor
public class TimeTableService {
    private final TimeTableRepository timeTableRepository;

    @Transactional
    public void deleteTimeTable(Long timetableId) {
        Long id = timeTableRepository.deleteByIdCustom(timetableId);

        if (id == 0) {
            throw new NoDeletedRowException("No deleted row found");
        }
    }
}
