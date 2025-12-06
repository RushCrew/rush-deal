package com.rushcrew.order_service.application.saga.step;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.rushcrew.order_service.application.command.dto.command.CreateOrderCommand;
import com.rushcrew.order_service.application.port.out.PointPort;
import com.rushcrew.order_service.application.saga.dto.OrderCreationSagaData;
import com.rushcrew.order_service.application.saga.dto.SagaContext;
import com.rushcrew.order_service.application.saga.dto.SagaStepResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeductPointStep {

	private final PointPort pointPort;

	/**
	 * Forward Transaction: 포인트 차감
	 */
	public SagaStepResult execute(SagaContext context, OrderCreationSagaData data) {
		try {
			log.info("[Saga-{}] DeductPoint 실행 시작", context.getSagaId());

			CreateOrderCommand command = data.getCommand();
			BigDecimal pointUsed = command.pointUsed();

			// 포인트 사용량이 0이면 스킵
			if (pointUsed == null || pointUsed.compareTo(BigDecimal.ZERO) == 0) {
				log.info("[Saga-{}] 포인트 사용량이 0이므로 스킵", context.getSagaId());
				return SagaStepResult.success();
			}

			// 포인트 차감 API 호출
			try {
				boolean deducted = pointPort.deductPoint(
					command.userId(),
					pointUsed,
					context.getSagaId().toString(), // 주문 ID 대신 Saga ID 사용
					"주문 결제"
				);

				if (!deducted) {
					log.error("[Saga-{}] 포인트 차감 실패: userId={}, amount={}",
						context.getSagaId(), command.userId(), pointUsed);
					return SagaStepResult.failure("포인트 잔액이 부족합니다.");
				}

				// Context에 포인트 차감 정보 저장 (보상용)
				context.setData("pointDeducted", true);
				context.setData("pointAmount", pointUsed);

				log.info("[Saga-{}] DeductPoint 실행 완료: userId={}, amount={}",
					context.getSagaId(), command.userId(), pointUsed);

				Map<String, Object> resultData = new HashMap<>();
				resultData.put("pointDeducted", true);
				resultData.put("pointAmount", pointUsed);

				return SagaStepResult.success(resultData);

			} catch (Exception e) {
				log.error("[Saga-{}] 포인트 차감 API 호출 실패", context.getSagaId(), e);
				return SagaStepResult.failure("포인트 차감 중 오류 발생: " + e.getMessage());
			}

		} catch (Exception e) {
			log.error("[Saga-{}] DeductPoint 실행 중 예외 발생", context.getSagaId(), e);
			return SagaStepResult.failure("포인트 차감 실패: " + e.getMessage());
		}
	}

	/**
	 * Compensating Transaction: 포인트 환불
	 */
	public void compensate(SagaContext context, OrderCreationSagaData data) {
		try {
			log.info("[Saga-{}] DeductPoint 보상 트랜잭션 시작", context.getSagaId());

			Boolean pointDeducted = (Boolean) context.getData("pointDeducted");
			if (pointDeducted == null || !pointDeducted) {
				log.warn("[Saga-{}] 차감된 포인트가 없어서 보상 트랜잭션 스킵", context.getSagaId());
				return;
			}

			BigDecimal pointAmount = (BigDecimal) context.getData("pointAmount");
			Long userId = data.getCommand().userId();

			// 포인트 환불 API 호출
			pointPort.refundPoint(
				userId,
				pointAmount,
				context.getSagaId().toString(),
				"주문 취소로 인한 포인트 환불"
			);

			log.info("[Saga-{}] DeductPoint 보상 트랜잭션 완료: userId={}, amount={}",
				context.getSagaId(), userId, pointAmount);

		} catch (Exception e) {
			log.error("[Saga-{}] DeductPoint 보상 트랜잭션 실패", context.getSagaId(), e);
			// 보상 실패 시 알림 필요
		}
	}
}
