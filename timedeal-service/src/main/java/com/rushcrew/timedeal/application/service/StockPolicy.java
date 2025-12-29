package com.rushcrew.timedeal.application.service;

import com.rushcrew.common.exception.BusinessException;
import com.rushcrew.timedeal.domain.entity.StockLog;
import com.rushcrew.timedeal.domain.entity.TimeDealStock;
import com.rushcrew.timedeal.domain.exception.TimeDealErrorCode;
import com.rushcrew.timedeal.domain.repository.StockRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockPolicy {

    private final StockRepository stockRepository;

    public TimeDealStock getStockOrThrow(UUID stockId) {
        return stockRepository.findNotDeletedById(stockId)
            .orElseThrow(() -> new BusinessException(TimeDealErrorCode.NOT_FOUND_STOCK));
    }

    public StockLog getLastLogOrThrow(UUID stockId, UUID orderId) {
        return stockRepository.findLastByStockIdAndOrderId(stockId, orderId)
            .orElseThrow(() -> new BusinessException(TimeDealErrorCode.NOT_FOUND_ORDER));
    }
}
