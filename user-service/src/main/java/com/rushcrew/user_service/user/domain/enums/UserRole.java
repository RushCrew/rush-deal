package com.rushcrew.user_service.user.domain.enums;

import com.rushcrew.common.exception.BusinessException;
import com.rushcrew.user_service.user.domain.error.UserErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    USER("일반 사용자"),
    SELLER("판매자"),
    MASTER("마스터 관리자");

    private final String description;

    public static UserRole of(String role) {
        if (role == null || role.trim().isEmpty()) {
            throw new BusinessException(UserErrorCode.INVALID_USER_ROLE);
        }

        for (UserRole value : UserRole.values()) {
            if (value.name().equalsIgnoreCase(role.trim())) {
                return value;
            }
        }
        throw new BusinessException(UserErrorCode.INVALID_USER_ROLE);
    }
}
