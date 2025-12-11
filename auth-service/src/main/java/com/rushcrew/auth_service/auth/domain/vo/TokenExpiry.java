package com.rushcrew.auth_service.auth.domain.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rushcrew.auth_service.auth.domain.exception.AuthErrorCode;
import com.rushcrew.common.exception.BusinessException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenExpiry {

    private final LocalDateTime expiresAt;

    public static TokenExpiry create(LocalDateTime expiresAt) {
        if (expiresAt == null) {
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN_EXPIRY);
        }

        if (expiresAt.isBefore(LocalDateTime.now())) {
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN_EXPIRY);
        }

        return new TokenExpiry(expiresAt.truncatedTo(ChronoUnit.SECONDS));
    }

    public static TokenExpiry fromMilliseconds(long millis) {
        if (millis <= 0) {
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN_EXPIRY);
        }

        LocalDateTime expiresAt = LocalDateTime.now().plus(
            millis,
            ChronoUnit.MILLIS
        );
        return create(expiresAt);
    }

    @JsonCreator
    public static TokenExpiry fromJson(
        @JsonProperty("expiresAt") LocalDateTime expiresAt
    ) {
        if (expiresAt == null) {
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN_EXPIRY);
        }
        return new TokenExpiry(expiresAt.truncatedTo(ChronoUnit.SECONDS));
    }

    @JsonIgnore
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
