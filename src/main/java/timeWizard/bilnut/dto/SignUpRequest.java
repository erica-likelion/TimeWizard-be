package timeWizard.bilnut.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignUpRequest {
    
    private String email;
    private String password;
    private String nickname;
    private String phoneNumber;
    private String university;
    private String major;
    private Integer grade;
}