package com.rushcrew.timedeal.infrastructure.event;

import com.rushcrew.timedeal.application.event.StockChangedEvent;
import com.rushcrew.timedeal.application.event.StockCreatedEvent;
import com.rushcrew.timedeal.application.event.StockDeletedEvent;
import com.rushcrew.timedeal.application.event.StockReservedEvent;
import com.rushcrew.timedeal.application.event.StockRestoredEvent;
import com.rushcrew.timedeal.domain.port.StockCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockEventHandler {

    private final StockCache stockCache;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStockCreated(StockCreatedEvent event) {
        try {
            stockCache.register(event.stockId(), event.totalStock());
        } catch (Exception e) {
            log.error(
                "StockCreatedEvent 처리 중 Redis 반영 실패. stock={}, totalStock={}",
                event.stockId(), event.totalStock(), e
            );
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStockChanged(StockChangedEvent event) {
        try {
            stockCache.increase(event.stockId(), event.quantity());
        } catch (Exception e) {
            log.error(
                "StockChangedEvent 처리 중 Redis 반영 실패. stock={}, quantity={}",
                event.stockId(), event.quantity(), e
            );
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStockDeleted(StockDeletedEvent event) {
        try {
            stockCache.delete(event.stockId());
        } catch (Exception e) {
            log.error("StockDeletedEvent 처리 중 Redis 반영 실패. stock={}", event.stockId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStockReserved(StockReservedEvent event) {
        try {
            stockCache.decrease(event.stockId(), event.quantity());
        } catch (Exception e) {
            log.error(
                "StockReservedEvent 처리 중 Redis 반영 실패. stock={}, quantity={}",
                event.stockId(), event.quantity(), e
            );
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStockRestored(StockRestoredEvent event) {
        try {
            stockCache.increase(event.stockId(), event.quantity());
        } catch (Exception e) {
            log.error(
                "StockRestoredEvent 처리 중 Redis 반영 실패. stock={}, quantity={}",
                event.stockId(), event.quantity(), e
            );
        }
    }
}
