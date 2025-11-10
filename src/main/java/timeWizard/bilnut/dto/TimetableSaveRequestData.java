package timeWizard.bilnut.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
public class TimetableSaveRequestData {
    private List<Long> courseIds;

    private String name;

    private String aiComment;

    private String uuidKey;
}
