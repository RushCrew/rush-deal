package com.rushcrew.order_service.presentation.mapper;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.rushcrew.order_service.application.command.dto.command.UpdateOrderCommand;
import com.rushcrew.order_service.domain.vo.ShippingInfo;
import com.rushcrew.order_service.presentation.dto.request.UpdateOrderRequest;

@Component
public class UpdateOrderCommandMapper {

	public UpdateOrderCommand toCommand(UUID orderId, Long userId, UpdateOrderRequest request) {
		ShippingInfo shippingInfo = null;
		if (request.shippingInfo() != null) {
			var s = request.shippingInfo();
			shippingInfo = ShippingInfo.builder()
				.recipientName(s.recipientName())
				.recipientPhone(s.recipientPhone())
				.zipCode(s.zipCode())
				.addressBase(s.addressBase())
				.addressDetail(s.addressDetail())
				.deliveryMessage(s.deliveryMessage())
				.build();
		}

		return UpdateOrderCommand.builder()
			.orderId(orderId)
			.userId(userId)
			.shippingInfo(shippingInfo)
			.pointUsed(request.pointUsed())
			.build();
	}
}
