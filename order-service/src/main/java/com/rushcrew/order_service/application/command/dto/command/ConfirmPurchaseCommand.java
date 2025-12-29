package com.rushcrew.order_service.application.command.dto.command;

import java.util.UUID;

import lombok.Builder;

@Builder
public record ConfirmPurchaseCommand(
	UUID orderId,
	Long userId
) {
	public static ConfirmPurchaseCommand of(UUID orderId, Long userId) {
		return new ConfirmPurchaseCommand(orderId, userId);
	}
}
