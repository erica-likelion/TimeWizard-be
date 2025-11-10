package timeWizard.bilnut.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    @Value("${cookie.secure:true}")
    private boolean cookieSecure;

    @Value("${cookie.refresh-token-max-age:259200}")
    private int refreshTokenMaxAge;

    public void setRefreshToken(HttpServletResponse response, String refreshToken) {
        Cookie refreshCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(cookieSecure);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(refreshTokenMaxAge);
        response.addCookie(refreshCookie);
    }

    public void deleteRefreshToken(HttpServletResponse response) {
        Cookie refreshCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(cookieSecure);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0); // 즉시 만료
        response.addCookie(refreshCookie);
    }
}