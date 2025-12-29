package com.rushcrew.timedeal.domain.port;

import java.util.UUID;

public interface StockCache {

    /**
     * Redis에 타임딜 재고 id, 구매가능한 quantity 캐시
     *
     * @param stockId   : Redis key에 사용되는 타임딜 재고 ID
     * @param available : 구매 가능한 재고 수량
     */
    void register(UUID stockId, Long available);

    /**
     * Redis에 캐시된 타임딜 재고 정보 삭제(무효화)
     *
     * @param stockId : Redis key에 사용되는 타임딜 재고 ID
     */
    void delete(UUID stockId);

    /**
     * Redis에 캐시된 재고 수량 차감
     *
     * @param stockId  : Redis key에 사용되는 타임딜 재고 ID
     * @param quantity : value에서 차감할 재고 수량 (예약된 재고 수량)
     */
    boolean decrease(UUID stockId, Long quantity);

    void increase(UUID stockId, Long quantity);
}
