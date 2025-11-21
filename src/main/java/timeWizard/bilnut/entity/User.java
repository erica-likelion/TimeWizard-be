package timeWizard.bilnut.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(length = 255, unique = true, nullable = false)
    private String email;

    @Column(length = 255, nullable = false)
    private String password;

    @Column(length = 50, nullable = false)
    private String nickname;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 100, nullable = false)
    private String university;

    @Column(length = 100, nullable = false)
    private String major;

    @Column(nullable = false)
    private Integer grade;

    @Column(nullable = true)
    private Integer graduationCredits;

    @Column(nullable = true)
    private Integer completedCredits;

    @Column(columnDefinition = "TEXT")
    private String preferences;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RefreshToken> refreshTokens;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @Builder
    public User(String email, String password, String nickname, String phoneNumber,
                String university, String major, Integer grade) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.university = university;
        this.major = major;
        this.grade = grade;
    }

    // 회원 정보 수정 메서드
    public void updateUserInfo(String nickname, String phoneNumber, String major, Integer grade, 
                               Integer graduationCredits, Integer completedCredits) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }
        if (major != null) {
            this.major = major;
        }
        if (grade != null) {
            this.grade = grade;
        }
        if (graduationCredits != null) {
            this.graduationCredits = graduationCredits;
        }
        if (completedCredits != null) {
            this.completedCredits = completedCredits;
        }
    }

    // 비밀번호 변경 메서드
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // 선호도 저장/수정 메서드
    public void updatePreferences(String preferences) {
        this.preferences = preferences;
    }
}
