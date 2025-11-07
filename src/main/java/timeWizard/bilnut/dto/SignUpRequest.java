package timeWizard.bilnut.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignUpRequest {
    
    private String loginId;
    private String email;
    private String password;
    private String nickname;
    private String university;
    private String major;
    private Integer grade;
    private String userPreferences;
    private Integer totalRequiredCredit;
    private Integer majorRequiredCredit;
    private Integer generalRequiredCredit;
}