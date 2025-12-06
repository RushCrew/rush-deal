package com.rushcrew.order_service.application.command.dto.result;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.Builder;

@Builder
public record CreateOrderResult(
	UUID orderId,
	Long userId,
	String orderStatus,
	BigDecimal totalAmount,
	BigDecimal pointUsed,
	BigDecimal finalAmount,
	Instant orderedAt,
	Instant reservationExpiresAt,	// 예약 만료 시간: 15분 후
	List<OrderItemResult> orderItems
) {

	@Builder
	public record OrderItemResult(
		UUID orderItemId,
		UUID timeDealStockId,
		UUID timeDealId,
		UUID productId,
		String productName,
		String optionName,
		Integer quantity,
		BigDecimal unitPrice,
		BigDecimal discountPrice,
		BigDecimal subtotal
	) {}
}
