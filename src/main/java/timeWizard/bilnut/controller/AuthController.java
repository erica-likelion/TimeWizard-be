package timeWizard.bilnut.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import timeWizard.bilnut.dto.LoginRequest;
import timeWizard.bilnut.dto.LoginResponse;
import timeWizard.bilnut.dto.SignUpRequest;
import timeWizard.bilnut.dto.SignUpResponse;
import timeWizard.bilnut.dto.TokenRefreshResponse;
import timeWizard.bilnut.service.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<SignUpResponse> signUp(@RequestBody SignUpRequest request) {
        SignUpResponse response = authService.signUp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request);
        
        // Refresh Token을 HttpOnly 쿠키로 설정
        Cookie refreshCookie = new Cookie("refreshToken", authService.getLastRefreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true); // HTTPS 사용 시
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7일
        response.addCookie(refreshCookie);
        
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 Refresh Token 추출
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        
        if (refreshToken == null) {
            throw new RuntimeException("Refresh token not found in cookies");
        }
        
        TokenRefreshResponse tokenResponse = authService.refreshAccessToken(refreshToken);
        
        // 새로운 Refresh Token을 쿠키로 설정
        Cookie refreshCookie = new Cookie("refreshToken", authService.getLastRefreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(refreshCookie);
        
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@AuthenticationPrincipal UserDetails userDetails, HttpServletResponse response) {
        authService.logout(userDetails.getUsername());
        
        // Refresh Token 쿠키 삭제
        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0); // 즉시 만료
        response.addCookie(refreshCookie);
        
        return ResponseEntity.ok("Logout successful");
    }
}
