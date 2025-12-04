package com.rushcrew.user_service.user.application.command;

public record UserUpdateCommand(
    Long userId,
    String name,
    String password
) {}
