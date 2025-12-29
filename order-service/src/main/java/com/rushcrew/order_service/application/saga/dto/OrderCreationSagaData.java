package com.rushcrew.order_service.application.saga.dto;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rushcrew.order_service.application.command.dto.command.CreateOrderCommand;
import com.rushcrew.order_service.domain.vo.ProductSnapshot;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)	// 알 수 없는 필드는 무시하고 필요한 필드만 역직렬화
public class OrderCreationSagaData {

	/**
	 * 주문 요청 원본
	 */
	private CreateOrderCommand command;

	/**
	 * ValidateStockStep에서 확정된 상품 스냅샷
	 * (타임딜, 옵션, 가격, 할인 정보 포함)
	 */
	private List<ProductSnapshot> productSnapshots;

	/**
	 * 임시 주문 ID (포인트 사용 시 생성)
	 * - 실제 주문 생성 전에 포인트 차감용으로 생성
	 * - 포인트 히스토리와 연결하기 위한 임시 ID
	 */
	// private String tempOrderId;

	/**
	 * 주문 생성 결과 (실제 주문 ID)
	 */
	private UUID orderId;
	private String queueToken;

	/* 주문 ID 설정 (호환성 유지) */
	public void bindOrderId(UUID orderId) {
		this.orderId = orderId;
	}

	/* 주문이 생성되었는지 확인 */
	public boolean isOrderCreated() {
		return orderId != null;
	}

	/**
	 * 포인트 사용 여부 확인
	 */
	public boolean hasPointUsage() {
		return command != null
			&& command.pointUsed() != null
			&& command.pointUsed() > 0;
	}
}
