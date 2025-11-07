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

    // 로그인
    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            // 1. loginId/비밀번호 기반 인증
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLoginId(), request.getPassword())
            );

            // 2. 인증 성공 시 사용자 조회
            User user = userRepository.findByLoginId(request.getLoginId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 3. Access Token 생성
            String accessToken = jwtTokenProvider.generateAccessToken(user.getLoginId());

            // 4. Refresh Token 생성
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getLoginId());

            // 5. Refresh Token을 DB에 저장 (기존 토큰이 있으면 삭제 후 저장)
            refreshTokenRepository.findByLoginId(user.getLoginId())
                    .ifPresent(refreshTokenRepository::delete);

            RefreshToken refreshTokenEntity = RefreshToken.builder()
                    .token(refreshToken)
                    .loginId(user.getLoginId())
                    .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                    .build();

            refreshTokenRepository.save(refreshTokenEntity);

            // 6. 로그인 응답 반환
            return LoginResponse.of(accessToken, refreshToken, accessTokenExpiration / 1000);

        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid loginId or password");
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
    public void logout(String loginId) {
        refreshTokenRepository.findByLoginId(loginId)
                .ifPresent(refreshTokenRepository::delete);
    }
}
