package com.rushcrew.user_service.user.presentation.dto.request;

import com.rushcrew.user_service.user.application.command.UserUpdateCommand;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    String name,

    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
    String password
) {
    public UserUpdateCommand toCommand(Long userId) {
        return new UserUpdateCommand(userId, name, password);
    }
}
