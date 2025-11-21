package timeWizard.bilnut.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PasswordChangeResponse {
    // 비밀번호 변경은 data 필드 없이 success와 message만 반환
}

