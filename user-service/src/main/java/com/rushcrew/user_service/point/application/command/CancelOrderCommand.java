package com.rushcrew.user_service.point.application.command;

import java.util.UUID;

public record CancelOrderCommand(
    Long userId,
    UUID orderId
) {}
