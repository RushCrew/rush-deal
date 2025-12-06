package com.rushcrew.order_service.application.command.dto.command;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.rushcrew.order_service.domain.vo.ShippingInfo;

import lombok.Builder;

@Builder
public record CreateOrderCommand(
	Long userId,
	UUID timeDealId,
	List<OrderItemCommand> orderItems,
	BigDecimal pointUsed,
	ShippingInfo shippingInfo
) {

	@Builder
	public record OrderItemCommand(
		UUID timeDealStockId,
		Integer quantity
	) {}
}
