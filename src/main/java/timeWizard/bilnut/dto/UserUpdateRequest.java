package timeWizard.bilnut.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserUpdateRequest {

    private String nickname;
    
    @JsonProperty("phone_number")
    private String phoneNumber;
    private String major;
    private Integer grade;
    
    @JsonProperty("graduation_credits")
    private Integer graduationCredits;
    
    @JsonProperty("completed_credits")
    private Integer completedCredits;
}

