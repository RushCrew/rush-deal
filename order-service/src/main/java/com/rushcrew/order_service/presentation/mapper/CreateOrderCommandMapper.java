package com.rushcrew.order_service.presentation.mapper;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.rushcrew.order_service.application.command.dto.command.CreateOrderCommand;
import com.rushcrew.order_service.presentation.dto.request.CreateOrderRequest;

@Component
public class CreateOrderCommandMapper {

	public CreateOrderCommand toCommand(
		CreateOrderRequest request,
		Long userId,
		String role,
		String queueToken
	) {
		return new CreateOrderCommand(
			userId,
			UUID.fromString(request.timeDealId()),
			UUID.fromString(request.productId()),
			queueToken,
			role,
			request.orderItems().stream()
				.map(i -> new CreateOrderCommand.OrderItemCommand(
					UUID.fromString(i.timeDealStockId()),
					i.quantity()
				))
				.toList(),
			request.pointUsed(),
			new CreateOrderCommand.ShippingInfoCommand(
				request.shippingInfo().recipientName(),
				request.shippingInfo().recipientPhone(),
				request.shippingInfo().zipCode(),
				request.shippingInfo().addressBase(),
				request.shippingInfo().addressDetail(),
				request.shippingInfo().deliveryMessage()
			)
		);
	}
}
