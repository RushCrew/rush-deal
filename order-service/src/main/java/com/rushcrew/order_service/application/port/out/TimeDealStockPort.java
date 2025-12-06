package com.rushcrew.order_service.application.port.out;

import java.util.UUID;

import com.rushcrew.order_service.application.port.dto.StockReservationResult;
import com.rushcrew.order_service.application.port.dto.TimeDealInfo;
import com.rushcrew.order_service.application.port.dto.TimeDealStockDetail;

import lombok.NonNull;

public interface TimeDealStockPort {

	TimeDealInfo getTimeDeal(
		@NonNull UUID timeDealId);

	// 타임딜 재고 정보 조회 (상품 ID, 옵션 ID 포함), 상품명, 상품설명, 판매자 정보 등도 함께 반환받아야 됨
	TimeDealStockDetail getTimeDealStockDetail(
		@NonNull UUID timeDealStockId);

	// 재고 예약 요청
	StockReservationResult reserveStock(
		@NonNull UUID timeDealStockId,
		@NonNull Integer quantity,
		@NonNull Long userId);

	// 재고 확정(= 판매 완료) -> 결제 완료 후 재고 예약 확정
	void confirmStock(
		@NonNull UUID timeDealStockId,
		@NonNull Integer quantity,
		@NonNull UUID orderId);

	// 재고 복구 (예약 해제)
	void restoreStock(
		@NonNull UUID timeDealStockId,
		@NonNull Integer quantity,
		@NonNull UUID orderId,
		@NonNull String reason);
}
