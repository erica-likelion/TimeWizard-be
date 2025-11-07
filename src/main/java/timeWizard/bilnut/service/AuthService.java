package timeWizard.bilnut.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import timeWizard.bilnut.config.JwtTokenProvider;
import timeWizard.bilnut.dto.LoginRequest;
import timeWizard.bilnut.dto.LoginResponse;
import timeWizard.bilnut.dto.SignUpRequest;
import timeWizard.bilnut.dto.SignUpResponse;
import timeWizard.bilnut.dto.TokenRefreshResponse;
import timeWizard.bilnut.entity.RefreshToken;
import timeWizard.bilnut.entity.User;
import timeWizard.bilnut.repository.RefreshTokenRepository;
import timeWizard.bilnut.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    // 회원가입
    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        // 1. 중복 체크
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. 사용자 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .phoneNumber(request.getPhoneNumber())
                .university(request.getUniversity())
                .major(request.getMajor())
                .grade(request.getGrade())
                .build();

        // 4. 사용자 저장
        User savedUser = userRepository.save(user);

        // 5. 응답 반환
        return SignUpResponse.of(
                savedUser.getUserId(),
                savedUser.getEmail(),
                savedUser.getNickname()
        );
    }

    // 로그인
    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            // 1. email/비밀번호 기반 인증
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // 2. 인증 성공 시 사용자 조회
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 3. Access Token 생성
            String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail());

            // 4. Refresh Token 생성
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

            // 5. Refresh Token을 DB에 저장 (기존 토큰이 있으면 삭제 후 저장)
            refreshTokenRepository.findByLoginId(user.getEmail())
                    .ifPresent(refreshTokenRepository::delete);

            RefreshToken refreshTokenEntity = RefreshToken.builder()
                    .token(refreshToken)
                    .loginId(user.getEmail())
                    .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                    .build();

            refreshTokenRepository.save(refreshTokenEntity);

            // 6. 로그인 응답 반환
            return LoginResponse.of(accessToken, refreshToken, accessTokenExpiration / 1000);

        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid email or password");
        }
    }

    // Refresh Token으로 새로운 Access Token 발급
    @Transactional
    public TokenRefreshResponse refreshAccessToken(String refreshToken) {
        // 1. Refresh Token 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        // 2. DB에서 Refresh Token 조회
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        // 3. 만료 여부 확인
        if (storedToken.isExpired()) {
            refreshTokenRepository.delete(storedToken);
            throw new RuntimeException("Refresh token expired");
        }

        // 4. 새로운 Access Token 생성
        String newAccessToken = jwtTokenProvider.generateAccessToken(storedToken.getLoginId());

        // 5. 새로운 Refresh Token 생성 (선택사항 - Refresh Token도 갱신)
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(storedToken.getLoginId());

        // 6. 기존 Refresh Token 삭제 후 새로운 토큰 저장
        refreshTokenRepository.delete(storedToken);

        RefreshToken newRefreshTokenEntity = RefreshToken.builder()
                .token(newRefreshToken)
                .loginId(storedToken.getLoginId())
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                .build();

        refreshTokenRepository.save(newRefreshTokenEntity);

        return TokenRefreshResponse.of(newAccessToken, newRefreshToken, accessTokenExpiration / 1000);
    }

    @Transactional
    public void logout(String email) {
        refreshTokenRepository.findByLoginId(email)
                .ifPresent(refreshTokenRepository::delete);
    }
}
