package com.rushcrew.queue.presentation.controller;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/queues")
@RequiredArgsConstructor
public class RedisCheckController {
    private final StringRedisTemplate redisTemplate;

    @GetMapping("/redis/check/{key}")
    public String getValue(@PathVariable String key) {
        return "Value: " + redisTemplate.opsForValue().get(key);
    }

    @DeleteMapping("/redis/clear")
    public ResponseEntity<String> clearRedis() {
        // RedisTemplate 사용
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().flushAll();
        return ResponseEntity.ok("Redis Cleaned!");
    }
}