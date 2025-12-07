package com.rushcrew.user_service.user.domain.enums;

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
            throw new IllegalArgumentException("권한은 필수입니다");
        }

        for (UserRole value : UserRole.values()) {
            if (value.name().equalsIgnoreCase(role.trim())) {
                return value;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 권한입니다: " + role);
    }
}
