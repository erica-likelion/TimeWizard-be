package timeWizard.bilnut.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import timeWizard.bilnut.entity.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
public class UserInfoResponse {

    @JsonProperty("user_id")
    private Long userId;
    private String email;
    private String nickname;
    
    @JsonProperty("phone_number")
    private String phoneNumber;
    private String university;
    private String major;
    private Integer grade;
    
    @JsonProperty("graduation_credits")
    private Integer graduationCredits;
    
    @JsonProperty("completed_credits")
    private Integer completedCredits;
    
    @JsonProperty("created_at")
    private String createdAt;

    public static UserInfoResponse from(User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return UserInfoResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .phoneNumber(user.getPhoneNumber())
                .university(user.getUniversity())
                .major(user.getMajor())
                .grade(user.getGrade())
                .graduationCredits(user.getGraduationCredits())
                .completedCredits(user.getCompletedCredits())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().format(formatter) : null)
                .build();
    }
}

