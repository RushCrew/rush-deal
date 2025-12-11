package com.rushcrew.user_service.point.application.command;

import java.util.UUID;

public record UsePointCommand(
    Long userId,
    UUID orderId,
    Long amount
) {}
