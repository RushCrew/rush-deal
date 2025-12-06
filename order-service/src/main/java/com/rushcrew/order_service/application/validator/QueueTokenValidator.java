package com.rushcrew.order_service.application.validator;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.rushcrew.common.exception.BusinessException;
import com.rushcrew.order_service.application.port.out.QueuePort;
import com.rushcrew.order_service.global.error.OrderErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class QueueTokenValidator {
	private final QueuePort queuePort;

	public void validate(UUID timeDealId, Long userId) {
		if (!queuePort.validateToken(timeDealId, userId)) {
			throw new BusinessException(OrderErrorCode.INVALID_QUEUE_TOKEN);
		}
	}
}
