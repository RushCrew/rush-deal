package com.rushcrew.auth_service.auth.presentation.util;

import com.rushcrew.auth_service.auth.application.policy.TokenPolicy;
import com.rushcrew.auth_service.auth.domain.exception.AuthErrorCode;
import com.rushcrew.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenUtils {

    private final TokenPolicy tokenPolicy;
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Refresh Token 쿠키 생성
     */
    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refresh_token", refreshToken)
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .path("/")
            .maxAge(tokenPolicy.refreshExpirationMillis() / 1000) // ms -> s 변환
            .build();
    }

    /**
     * 만료된 쿠키 생성
     */
    public ResponseCookie createExpiredRefreshTokenCookie() {
        return ResponseCookie.from("refresh_token", "")
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .path("/")
            .maxAge(0) // 즉시 만료
            .build();
    }

    public String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
            if ("refresh_token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public String extractAccessToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
        }
        return authorizationHeader.substring(BEARER_PREFIX.length());
    }
}