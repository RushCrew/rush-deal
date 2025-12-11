package com.rushcrew.auth_service.auth.domain.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rushcrew.auth_service.auth.domain.exception.AuthErrorCode;
import com.rushcrew.common.exception.BusinessException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenId {

    private final String value;

    public static TokenId of(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN_ID);
        }
        return new TokenId(value);
    }

    @JsonCreator
    public static TokenId fromJson(@JsonProperty("value") String value) {
        return of(value);
    }
}
