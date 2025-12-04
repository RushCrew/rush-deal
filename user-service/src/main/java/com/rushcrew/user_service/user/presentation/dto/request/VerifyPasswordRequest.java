package com.rushcrew.user_service.user.presentation.dto.request;

import com.rushcrew.user_service.user.application.command.VerifyPasswordCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyPasswordRequest(
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    String email,

    @NotBlank(message = "비밀번호는 필수입니다") String password
) {
    public VerifyPasswordCommand toCommand() {
        return new VerifyPasswordCommand(email, password);
    }
}
