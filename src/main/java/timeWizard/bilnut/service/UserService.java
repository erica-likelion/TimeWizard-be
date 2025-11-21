package timeWizard.bilnut.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import timeWizard.bilnut.dto.*;
import timeWizard.bilnut.entity.User;
import timeWizard.bilnut.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원 정보 조회
    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return UserInfoResponse.from(user);
    }

    // 회원 정보 수정
    @Transactional
    public UserUpdateResponse updateUserInfo(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.updateUserInfo(
                request.getNickname(),
                request.getPhoneNumber(),
                request.getMajor(),
                request.getGrade(),
                request.getGraduationCredits(),
                request.getCompletedCredits()
        );

        User updatedUser = userRepository.save(user);
        return UserUpdateResponse.from(updatedUser);
    }

    // 비밀번호 변경
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // 새 비밀번호 암호화 후 저장
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.updatePassword(encodedNewPassword);
        userRepository.save(user);
    }

    // 선호도 조회
    @Transactional(readOnly = true)
    public PreferencesResponse getPreferences(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return PreferencesResponse.from(user.getPreferences());
    }

    // 선호도 저장/수정
    @Transactional
    public PreferencesResponse updatePreferences(Long userId, PreferencesRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // PreferencesRequest를 JSON 문자열로 변환하여 저장
        PreferencesResponse preferencesResponse = PreferencesResponse.builder()
                .preferredDays(request.getPreferredDays())
                .preferredStartTime(request.getPreferredStartTime())
                .preferredEndTime(request.getPreferredEndTime())
                .targetCredits(request.getTargetCredits())
                .requiredCourses(request.getRequiredCourses())
                .excludedCourses(request.getExcludedCourses())
                .build();

        user.updatePreferences(preferencesResponse.toJson());
        userRepository.save(user);
        
        return preferencesResponse;
    }
}

