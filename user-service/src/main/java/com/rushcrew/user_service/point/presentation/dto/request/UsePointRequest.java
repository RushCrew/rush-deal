package com.rushcrew.user_service.point.presentation.dto.request;

import com.rushcrew.user_service.point.application.command.UsePointCommand;

public record UsePointRequest(
    Long userId,
    String orderId,
    Long amount,
    String sagaId

) {
    public UsePointCommand toCommand() {
        return new UsePointCommand(
            userId,
            orderId,
            amount,
            sagaId
            );
    }
}
