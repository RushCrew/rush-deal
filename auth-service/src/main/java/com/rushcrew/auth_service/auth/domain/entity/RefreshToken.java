package com.rushcrew.auth_service.auth.domain.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rushcrew.auth_service.auth.domain.exception.AuthErrorCode;
import com.rushcrew.auth_service.auth.domain.vo.TokenExpiry;
import com.rushcrew.auth_service.auth.domain.vo.TokenId;
import com.rushcrew.auth_service.auth.domain.vo.UserId;
import com.rushcrew.common.exception.BusinessException;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RefreshToken {

    private final TokenId id;
    private final UserId userId;
    private final LocalDateTime issuedAt;
    private final TokenExpiry expiry;

    public static RefreshToken create(
        String tokenValue,
        UserId userId,
        Long expiryMillis
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new RefreshToken(
            TokenId.of(tokenValue),
            userId,
            now,
            TokenExpiry.fromMilliseconds(expiryMillis)
        );
    }

    @JsonCreator
    public static RefreshToken fromJson(
        @JsonProperty("id") TokenId id,
        @JsonProperty("userId") UserId userId,
        @JsonProperty("issuedAt") LocalDateTime issuedAt,
        @JsonProperty("expiry") TokenExpiry expiry
    ) {
        return new RefreshToken(id, userId, issuedAt, expiry);
    }

    public boolean isExpired() {
        return expiry.isExpired();
    }

    public void ensureValid() {
        if (isExpired()) {
            throw new BusinessException(AuthErrorCode.TOKEN_EXPIRED);
        }
    }

    public Long getUserId() {
        return userId.getValue();
    }

    public String getTokenValue() {
        return id.getValue();
    }

}
