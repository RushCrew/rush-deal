package com.rushcrew.order_service.application.saga.step;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.rushcrew.order_service.application.port.out.PointPort;
import com.rushcrew.order_service.application.saga.dto.OrderCreationSagaData;
import com.rushcrew.order_service.application.saga.dto.SagaContext;
import com.rushcrew.order_service.application.saga.dto.SagaStepResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UsePointStep {

	private final PointPort pointPort;

	public SagaStepResult execute(SagaContext context, OrderCreationSagaData data) {
		try {
			Long pointUsed = data.getCommand().pointUsed();

			// 포인트 사용이 없으면 스킵
			if (pointUsed == null || pointUsed <= 0) {
				log.info("[Saga-{}] 포인트 사용 없음, Step 스킵", context.getSagaId());
				return SagaStepResult.success();
			}

			Long userId = context.getUserId();
			UUID sagaId = context.getSagaId();
			UUID orderId = data.getOrderId();

			log.info("[Saga-{}] 포인트 사용 요청: userId={}, pointUsed={}, orderId={}",
				sagaId, userId, pointUsed, orderId);

			pointPort.usePoint(userId, orderId.toString(), pointUsed, sagaId);

			log.info("[Saga-{}] 포인트 사용 완료: orderId={}, pointUsed={}",
				sagaId, orderId, pointUsed);

			return SagaStepResult.success();

		} catch (IllegalArgumentException e) {
			// 비즈니스 예외 (잔액 부족 등)
			log.warn("[Saga-{}] 포인트 사용 실패: {}", context.getSagaId(), e.getMessage());
			return SagaStepResult.failure(e.getMessage());
		} catch (Exception e) {
			// 시스템 예외
			log.error("[Saga-{}] 포인트 사용 실패: {}", context.getSagaId(), e.getMessage(), e);
			return SagaStepResult.failure("포인트 서비스 호출 실패: " + e.getMessage());
		}
	}

	/* 보상 트랜잭션: 포인트 복구*/
	public void compensate(SagaContext context, OrderCreationSagaData data) {
		try {
			Long pointUsed = data.getCommand().pointUsed();

			if (pointUsed == null || pointUsed == 0) {
				log.info("[Saga-{}] 포인트 사용 없음, 보상 트랜잭션 스킵", context.getSagaId());
				return;
			}

			UUID orderId = data.getOrderId();
			Long userId = context.getUserId();
			UUID sagaId = context.getSagaId();

			log.info("[Saga-{}] 포인트 보상 트랜잭션 시작: orderId={}, pointUsed={}",
				sagaId, orderId, pointUsed);

			pointPort.cancelPointUse(userId, orderId.toString(), sagaId);

			log.info("[Saga-{}] 포인트 보상 트랜잭션 완료", sagaId);

		} catch (Exception e) {
			log.error("[Saga-{}] 포인트 보상 트랜잭션 실패: {}",
				context.getSagaId(), e.getMessage(), e);
			// 보상 실패는 별도 모니터링/알림 필요
			throw new RuntimeException("포인트 보상 실패", e);
		}
	}
}
