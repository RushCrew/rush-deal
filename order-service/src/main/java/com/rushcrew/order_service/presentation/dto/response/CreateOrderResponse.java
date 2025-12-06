package com.rushcrew.order_service.presentation.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.rushcrew.order_service.application.command.dto.result.CreateOrderResult;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateOrderResponse {
	private UUID orderId;
	private String orderStatus;
	private BigDecimal totalAmount;
	private BigDecimal pointUsed;
	private BigDecimal finalAmount;
	private Instant orderedAt;
	private Instant reservationExpiresAt;
	private String message;
	private List<OrderItemResponse> orderItems;

	@Getter
	@AllArgsConstructor
	public static class OrderItemResponse {
		private UUID orderItemId;
		private String productName;
		private String optionName;
		private Integer quantity;
		private BigDecimal unitPrice;
		private BigDecimal discountPrice;
		private BigDecimal subtotal;
	}

	public static CreateOrderResponse from(CreateOrderResult result) {
		List<OrderItemResponse> items = result.orderItems().stream()
			.map(item -> new OrderItemResponse(
				item.orderItemId(),
				item.productName(),
				item.optionName(),
				item.quantity(),
				item.unitPrice(),
				item.discountPrice(),
				item.subtotal()
			))
			.toList();

		return new CreateOrderResponse(
			result.orderId(),
			result.orderStatus(),
			result.totalAmount(),
			result.pointUsed(),
			result.finalAmount(),
			result.orderedAt(),
			result.reservationExpiresAt(),
			"주문이 성공적으로 생성되었습니다. 15분 내에 결제를 완료해주세요.",
			items
		);
	}
}
