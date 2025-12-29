package com.rushcrew.order_service.infrastructure.adapter.out.messaging;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rushcrew.order_service.application.port.out.StockEventPort;
import com.rushcrew.order_service.infrastructure.messaging.event.OutboxEventType;
import com.rushcrew.order_service.infrastructure.persistence.outbox.entity.OutboxEventEntity;
import com.rushcrew.order_service.infrastructure.persistence.outbox.repository.OutboxEventJpaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockEventPublisher implements StockEventPort {

	private final OutboxEventJpaRepository outboxRepository;
	private final ObjectMapper objectMapper;

	@Override
	public void publishStockReservationCancelled(UUID orderId, UUID timeDealStockId, Long quantity, String reason) {
		try {
			log.info("재고 예약 취소 이벤트 발행: orderId={}, timeDealStockId={}, quantity={}",
				orderId, timeDealStockId, quantity);

			Map<String, Object> event = new HashMap<>();
			event.put("orderId", orderId.toString());
			event.put("stockId", timeDealStockId.toString());
			event.put("quantity", quantity);
			event.put("reason", reason);
			// event.put("timestamp", timestamp.toString());

			String payload = objectMapper.writeValueAsString(event);

			OutboxEventEntity outbox = OutboxEventEntity.create(
				"ORDER",         // aggregateType
				orderId,                       // aggregateId
				OutboxEventType.STOCK_RESERVATION_CANCELLED,
				payload                        // json
			);

			outboxRepository.save(outbox);
			log.info("재고 예약 취소 이벤트 Outbox 저장 완료: orderId={}, timeDealStockId={}",
				orderId, timeDealStockId);

		} catch (Exception e) {
			log.error("재고 예약 취소 이벤트 발행 실패: orderId={}, timeDealStockId={}",
				orderId, timeDealStockId, e);
			throw new RuntimeException("재고 예약 취소 이벤트 발행 실패", e);
		}
	}
}
