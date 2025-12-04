package com.rushcrew.user_service.user.presentation.dto.response;

import com.rushcrew.user_service.user.application.result.UserResult;

public record UserResponse(
    Long userId,
    String email,
    String name
) {
    public static UserResponse fromResult(UserResult result) {
        return new UserResponse(
            result.userId(),
            result.email(),
            result.name()
        );
    }
}
