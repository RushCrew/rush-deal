package com.rushcrew.timedeal.application.service.impl;

import com.rushcrew.common.enums.UserRole;
import com.rushcrew.common.exception.BusinessException;
import com.rushcrew.common.global.error.CommonErrorCode;
import com.rushcrew.timedeal.application.command.ConfirmStockCommand;
import com.rushcrew.timedeal.application.command.CreateStockCommand;
import com.rushcrew.timedeal.application.command.ReserveStockCommand;
import com.rushcrew.timedeal.application.command.RestoreStockCommand;
import com.rushcrew.timedeal.application.command.UpdateStockCountCommand;
import com.rushcrew.timedeal.application.event.StockChangedEvent;
import com.rushcrew.timedeal.application.event.StockCreatedEvent;
import com.rushcrew.timedeal.application.event.StockDeletedEvent;
import com.rushcrew.timedeal.application.event.StockRestoredEvent;
import com.rushcrew.timedeal.application.result.ConfirmStockResult;
import com.rushcrew.timedeal.application.result.CreateStockResult;
import com.rushcrew.timedeal.application.result.ReserveStockResult;
import com.rushcrew.timedeal.application.result.StockLogResult;
import com.rushcrew.timedeal.application.result.StockResult;
import com.rushcrew.timedeal.application.result.UpdateStockCountResult;
import com.rushcrew.timedeal.application.service.RetryStockService;
import com.rushcrew.timedeal.application.service.StockPolicy;
import com.rushcrew.timedeal.application.service.StockService;
import com.rushcrew.timedeal.domain.entity.StockLog;
import com.rushcrew.timedeal.domain.entity.TimeDealProduct;
import com.rushcrew.timedeal.domain.entity.TimeDealStock;
import com.rushcrew.timedeal.domain.exception.TimeDealErrorCode;
import com.rushcrew.timedeal.domain.port.StockCache;
import com.rushcrew.timedeal.domain.repository.StockRepository;
import com.rushcrew.timedeal.domain.repository.TimeDealRepository;
import com.rushcrew.timedeal.domain.vo.EventType;
import com.rushcrew.timedeal.domain.vo.OrderId;
import com.rushcrew.timedeal.domain.vo.TimeDealStockStatus;
import jakarta.persistence.OptimisticLockException;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final TimeDealRepository timeDealRepository;
    private final StockRepository stockRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final StockCache stockCache;

    private final StockPolicy stockPolicy;
    private final RetryStockService retryStockService;

    @Override
    @Transactional
    public CreateStockResult createStock(CreateStockCommand command) {
        TimeDealProduct timeDealProduct =
            timeDealRepository.findProductByProductId(command.productId())
                .orElseThrow(() -> new BusinessException(TimeDealErrorCode.NOT_FOUND_PRODUCT));

        TimeDealStock newStock = TimeDealStock.create(command, timeDealProduct);
        stockRepository.save(newStock);

        eventPublisher.publishEvent(new StockCreatedEvent(newStock.getId(), command.totalStock()));

        return CreateStockResult.of(newStock, command.totalStock());
    }

    @Override
    @Transactional
    public UpdateStockCountResult changeStockCount(UUID stockId, UpdateStockCountCommand command) {
        TimeDealStock stock = stockPolicy.getStockOrThrow(stockId);
        Long previousStock = stock.getStockCounts().getAvailable();

        Long quantity = command.quantity().getQuantity();
        stock.validQuantity(quantity);

        stock.changeAvailable(quantity, command.reason());

        eventPublisher.publishEvent(new StockChangedEvent(stockId, quantity));

        return UpdateStockCountResult.of(
            stock.getId(), stock.getTimeDealProduct().getId(),
            previousStock, stock.getStockCounts().getAvailable(), quantity
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockResult> getStocks(
        String keyword, UUID productId, TimeDealStockStatus status, Pageable pageable
    ) {
        String pattern = (keyword == null || keyword.isBlank()) ? null : "%" + keyword + "%";
        return stockRepository.findStockResults(pattern, productId, status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public StockResult getStock(Long userId, String role, UUID stockId) {
        StockResult result = stockRepository.findStockResultById(stockId);
        if (role.equals(UserRole.SELLER.getDescription())
            && !Objects.equals(userId, result.sellerId())) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN);
        }

        return result;
    }

    @Override
    @Transactional
    public void deleteStock(UUID stockId, Long userId) {
        TimeDealStock stock = stockPolicy.getStockOrThrow(stockId);

        stock.delete(userId);
        eventPublisher.publishEvent(new StockDeletedEvent(stockId));
    }

    @Override
    public ReserveStockResult reserveStock(ReserveStockCommand command) {
        UUID stockId = command.stockId();
        Long quantity = command.quantity().getQuantity();

        if (!stockCache.decrease(stockId, quantity)) {
            throw new BusinessException(TimeDealErrorCode.OUT_OF_STOCK);
        }

        try {
            return retryStockService.reserveWithRetry(command);
        } catch (OptimisticLockException e) {
            stockCache.increase(stockId, quantity);
            throw e;
        } catch (Exception e) {
            stockCache.increase(stockId, quantity);
            throw e;
        }
    }

    @Override
    @Transactional
    public ConfirmStockResult confirmStock(ConfirmStockCommand command) {
        UUID orderId = command.orderId().getOrderId();
        TimeDealStock stock = stockPolicy.getStockOrThrow(command.stockId());
        StockLog log = stockPolicy.getLastLogOrThrow(command.stockId(), orderId);
        log.validateOrderQuantity(command.quantity());

        stock.confirm(OrderId.of(orderId), command.quantity());

        return ConfirmStockResult.of(orderId);
    }

    @Override
    @Transactional
    public void restoreStock(RestoreStockCommand command) {
        UUID orderId = command.orderId().getOrderId();
        TimeDealStock stock = stockPolicy.getStockOrThrow(command.stockId());
        StockLog log = stockPolicy.getLastLogOrThrow(command.stockId(), orderId);
        log.validateOrderQuantity(command.quantity());

        if (log.getEventType() == EventType.RESERVE) {
            stock.restoreFromReserved(OrderId.of(orderId), command.quantity(), command.reason());
        } else if (log.getEventType() == EventType.SELL) {
            stock.restoreFromSold(OrderId.of(orderId), command.quantity(), command.reason());
        } else {
            throw new BusinessException(TimeDealErrorCode.INVALID_ORDER_STATE);
        }

        eventPublisher.publishEvent(
            new StockRestoredEvent(command.stockId(), command.quantity().getQuantity())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockLogResult> getStockLogs(UUID stockId, String eventType, Pageable pageable) {
        return stockRepository.findLogByIdAndFilter(stockId, eventType, pageable);
    }
}
