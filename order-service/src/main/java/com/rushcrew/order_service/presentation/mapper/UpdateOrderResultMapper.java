package com.rushcrew.order_service.presentation.mapper;

import org.springframework.stereotype.Component;

import com.rushcrew.order_service.application.command.dto.result.UpdateOrderResult;
import com.rushcrew.order_service.presentation.dto.response.UpdateOrderResponse;

@Component
public class UpdateOrderResultMapper {

	public UpdateOrderResponse toResponse(UpdateOrderResult result) {
		UpdateOrderResponse.ShippingInfoResponse shippingInfoResponse = null;

		if (result.shippingInfo() != null) {
			var s = result.shippingInfo();
			shippingInfoResponse = new UpdateOrderResponse.ShippingInfoResponse(
				s.recipientName(),
				s.recipientPhone(),
				s.zipCode(),
				s.addressBase(),
				s.addressDetail(),
				s.deliveryMessage()
			);
		}

		return new UpdateOrderResponse(
			result.orderId(),
			result.orderStatus(),
			shippingInfoResponse,
			result.pointUsed(),
			result.totalAmount(),
			result.finalAmount(),
			result.updatedAt(),
			"주문이 수정되었습니다."
		);
	}
}
