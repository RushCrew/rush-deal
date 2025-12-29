package com.rushcrew.order_service.application.command.mapper;

import org.springframework.stereotype.Component;

import com.rushcrew.order_service.application.command.dto.command.CreateOrderCommand;
import com.rushcrew.order_service.domain.vo.ShippingInfo;

@Component
public class CommandToDomainMapper {

	public ShippingInfo toShippingInfo(
		CreateOrderCommand.ShippingInfoCommand command
	) {
		return ShippingInfo.builder()
			.recipientName(command.recipientName())
			.recipientPhone(command.recipientPhone())
			.zipCode(command.zipCode())
			.addressBase(command.addressBase())
			.addressDetail(command.addressDetail())
			.deliveryMessage(command.deliveryMessage())
			.build();
	}
}
