package com.rushcrew.order_service.application.port.out;

import java.util.UUID;

import com.rushcrew.order_service.infrastructure.persistence.outbox.entity.OutboxEventEntity;

public interface OutboxPort {

	/**
	 * Outbox 이벤트 저장
	 */
	OutboxEventEntity save(OutboxEventEntity event);

	/**
	 * 이벤트 생성 및 저장 (편의 메서드)
	 */
	OutboxEventEntity createAndSave(
		String aggregateType,
		UUID aggregateId,
		String eventType,
		String payload
	);
}
