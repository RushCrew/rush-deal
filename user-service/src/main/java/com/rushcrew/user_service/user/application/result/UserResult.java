package com.rushcrew.user_service.user.application.result;

public record UserResult(
    Long userId,
    String email,
    String name
) {}