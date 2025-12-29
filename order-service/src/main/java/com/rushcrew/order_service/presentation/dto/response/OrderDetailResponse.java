package com.rushcrew.order_service.presentation.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.rushcrew.order_service.application.query.dto.OrderDetailDto;
import com.rushcrew.order_service.application.query.dto.OrderItemQueryDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrderDetailResponse {
	private UUID orderId;
	private Long userId;
	private String orderStatus;
	private BigDecimal totalAmount;
	private Long pointUsed;
	private BigDecimal finalAmount;
	private Instant orderedAt;
	private Instant paymentCompletedAt;
	private Instant purchaseConfirmedAt;
	private Instant cancelledAt;
	private Instant autoConfirmScheduledAt;
	private ShippingInfoResponse shippingInfo;
	private List<OrderItemResponse> orderItems;

	public static OrderDetailResponse from(OrderDetailDto dto) {
		return OrderDetailResponse.builder()
			.orderId(dto.getOrderId())
			.userId(dto.getUserId())
			.orderStatus(dto.getOrderStatus())
			.totalAmount(dto.getTotalAmount())
			.pointUsed(dto.getPointUsed())
			.finalAmount(dto.getFinalAmount())
			.orderedAt(dto.getOrderedAt())
			.paymentCompletedAt(dto.getPaymentCompletedAt())
			.purchaseConfirmedAt(dto.getPurchaseConfirmedAt())
			.cancelledAt(dto.getCancelledAt())
			.autoConfirmScheduledAt(dto.getAutoConfirmScheduledAt())
			.shippingInfo(dto.getShippingInfo() != null
				? ShippingInfoResponse.from(dto.getShippingInfo())
				: null)
			.orderItems(dto.getOrderItems() != null
				? dto.getOrderItems().stream()
				.map(OrderItemResponse::from)
				.collect(Collectors.toList())
				: List.of())
			.build();
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class OrderItemResponse {
		private UUID orderItemId;
		private String productName;
		private String optionName;
		private Long quantity;
		private BigDecimal unitPrice;
		private BigDecimal discountPrice;
		private BigDecimal subtotal;
		private BigDecimal discountRate;

		public static OrderItemResponse from(Object item) {
			if (item instanceof OrderItemQueryDto result) {
				return OrderItemResponse.builder()
					.orderItemId(result.orderItemId())
					.productName(result.productName())
					.optionName(result.optionName())
					.quantity(result.quantity())
					.unitPrice(result.unitPrice())
					.discountPrice(result.discountPrice())
					.subtotal(result.subtotal())
					.build();
			}
			return null;
		}
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class ShippingInfoResponse {
		private String recipientName;
		private String recipientPhone;
		private String zipCode;
		private String addressBase;
		private String addressDetail;
		private String deliveryMessage;

		public static ShippingInfoResponse from(com.rushcrew.order_service.domain.vo.ShippingInfo shippingInfo) {
			return ShippingInfoResponse.builder()
				.recipientName(shippingInfo.getRecipientName())
				.recipientPhone(shippingInfo.getRecipientPhone())
				.zipCode(shippingInfo.getZipCode())
				.addressBase(shippingInfo.getAddressBase())
				.addressDetail(shippingInfo.getAddressDetail())
				.deliveryMessage(shippingInfo.getDeliveryMessage())
				.build();
		}
	}
}
