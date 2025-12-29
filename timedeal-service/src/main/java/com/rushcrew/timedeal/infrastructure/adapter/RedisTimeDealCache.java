package com.rushcrew.timedeal.infrastructure.adapter;

import com.rushcrew.timedeal.domain.port.TimeDealCache;
import com.rushcrew.timedeal.domain.port.TimeDealQueueKey;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisTimeDealCache implements TimeDealCache {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void syncScheduleTime(UUID timeDealId, Instant time, TimeDealQueueKey queueKey) {
        redisTemplate.opsForZSet()
            .add(queueKey.getKey(), timeDealId.toString(), time.toEpochMilli());
    }

    @Override
    public List<String> getTimeDealIds(TimeDealQueueKey queueKey) {
        return redisTemplate.opsForZSet()
            .rangeByScoreWithScores(queueKey.getKey(), 0, Instant.now().toEpochMilli())
            .stream().map(timeDeal -> timeDeal.getValue())
            .collect(Collectors.toList());
    }

    @Override
    public void removeTimedOut(TimeDealQueueKey key, List<String> timeDealIds) {
        redisTemplate.opsForZSet().remove(key.getKey(), timeDealIds.toArray());
    }
}
