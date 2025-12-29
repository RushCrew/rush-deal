package com.rushcrew.order_service.application.command.dto.command;

import java.util.List;
import java.util.UUID;

public record CreateOrderCommand(
	Long userId,
	UUID timeDealId,
	UUID productId,
	String queueToken,
	String role,
	List<OrderItemCommand> orderItems,
	Long pointUsed,
	ShippingInfoCommand shippingInfo
) {
	public record OrderItemCommand(
		UUID timeDealStockId,
		Long quantity
	) {}

	public record ShippingInfoCommand(
		String recipientName,
		String recipientPhone,
		String zipCode,
		String addressBase,
		String addressDetail,
		String deliveryMessage
	) {}
}
