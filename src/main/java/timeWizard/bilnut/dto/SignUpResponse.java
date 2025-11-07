package timeWizard.bilnut.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignUpResponse {
    
    private Long userId;
    private String loginId;
    private String email;
    private String nickname;
    private String message;
    
    public static SignUpResponse of(Long userId, String loginId, String email, String nickname) {
        return SignUpResponse.builder()
                .userId(userId)
                .loginId(loginId)
                .email(email)
                .nickname(nickname)
                .message("회원가입이 성공적으로 완료되었습니다.")
                .build();
    }
}