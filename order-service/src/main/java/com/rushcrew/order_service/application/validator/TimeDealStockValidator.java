package com.rushcrew.order_service.application.validator;

import org.springframework.stereotype.Component;

import com.rushcrew.common.exception.BusinessException;
import com.rushcrew.order_service.application.port.dto.TimeDealStatus;
import com.rushcrew.order_service.application.port.dto.TimeDealStockDetail;
import com.rushcrew.order_service.application.port.dto.TimeDealStockStatus;
import com.rushcrew.order_service.global.error.OrderErrorCode;

@Component
public class TimeDealStockValidator {

	public void validate(TimeDealStockDetail stockDetail) {
		// 상품 활성 상태 확인
		if (!stockDetail.isActive()) {
			throw new BusinessException(OrderErrorCode.INVALID_PRODUCT);
		}
		// 재고 상태 확인
		if (stockDetail.getStatus() == TimeDealStockStatus.SOLD) {
			throw new BusinessException(OrderErrorCode.SOLD_OUT_PRODUCT);
		}
	}
}
