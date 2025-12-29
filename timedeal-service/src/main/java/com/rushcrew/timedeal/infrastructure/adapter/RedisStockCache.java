package com.rushcrew.timedeal.infrastructure.adapter;

import com.rushcrew.common.exception.BusinessException;
import com.rushcrew.timedeal.domain.exception.TimeDealErrorCode;
import com.rushcrew.timedeal.domain.port.StockCache;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisStockCache implements StockCache {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void register(UUID stockId, Long available) {
        String key = "tds:" + stockId;
        redisTemplate.opsForValue().set(key, String.valueOf(available));
    }

    @Override
    public void delete(UUID stockId) {
        redisTemplate.delete("tds:" + stockId);
    }

    /**
     * quantity가 양수면 증가, 음수면 감소
     */
    @Override
    public void increase(UUID stockId, Long quantity) {
        String key = "tds:" + stockId;
        redisTemplate.opsForValue().increment(key, quantity);
    }

    @Override
    public boolean decrease(UUID stockId, Long quantity) {
        String key = "tds:" + stockId;
        Long available = redisTemplate.opsForValue().decrement(key, quantity);

        if(available == null) {
            throw new BusinessException(TimeDealErrorCode.NOT_FOUND_STOCK);
        }

        if(available < 0) {
            redisTemplate.opsForValue().increment(key, quantity);
            return false;
        }

        return true;
    }
}
