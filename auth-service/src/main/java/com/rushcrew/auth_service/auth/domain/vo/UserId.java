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
public class UserId {

    private final Long value;

    public static UserId of(Long value) {
        if (value == null || value <= 0) {
            throw new BusinessException(AuthErrorCode.INVALID_USER_ID);
        }
        return new UserId(value);
    }

    @JsonCreator
    public static UserId fromJson(@JsonProperty("value") Long value) {
        return of(value);
    }
}
