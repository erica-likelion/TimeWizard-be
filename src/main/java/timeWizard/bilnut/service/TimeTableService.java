package timeWizard.bilnut.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import timeWizard.bilnut.config.exception.NoDeletedRowException;
import timeWizard.bilnut.dto.AiTimetableRequestData;
import timeWizard.bilnut.repository.TimeTableRepository;

@Service
@RequiredArgsConstructor
public class TimeTableService {
    private final TimeTableRepository timeTableRepository;
    private final WebClient aiApiWebClient;

    @Async("aiApiRequestExecutor")
    public String requestAiTimeTable(AiTimetableRequestData aiTimetableRequestData) { // 아직 로그인이 구현이 안돼서 더미 데이터로 구현

    }


    @Transactional
    public void deleteTimeTable(Long timetableId) {
        Long id = timeTableRepository.deleteByIdCustom(timetableId);

        if (id == 0) {
            throw new NoDeletedRowException("No deleted row found");
        }
    }
}
