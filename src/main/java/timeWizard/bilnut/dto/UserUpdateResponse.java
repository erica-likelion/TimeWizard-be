package timeWizard.bilnut.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import timeWizard.bilnut.entity.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
public class UserUpdateResponse {

    @JsonProperty("user_id")
    private Long userId;
    
    @JsonProperty("updated_at")
    private String updatedAt;

    public static UserUpdateResponse from(User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return UserUpdateResponse.builder()
                .userId(user.getUserId())
                .updatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().format(formatter) : null)
                .build();
    }
}

