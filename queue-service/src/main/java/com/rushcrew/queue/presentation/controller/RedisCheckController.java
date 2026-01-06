package com.rushcrew.queue.presentation.controller;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
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
        try {
            // 패턴 매칭으로 모든 키 찾기 ("*"은 모든 키)
            ScanOptions options = ScanOptions.scanOptions().match("*").count(1000).build();

            // Cursor를 사용하여 반복적으로 키를 조회 및 삭제 (메모리 부하 방지)
            Cursor<byte[]> cursor = redisTemplate.getConnectionFactory().getConnection().scan(options);

            int count = 0;
            while (cursor.hasNext()) {
                byte[] key = cursor.next();
                redisTemplate.delete(new String(key)); // 하나씩 삭제
                count++;
            }

            return ResponseEntity.ok("Redis Cleaned! Deleted " + count + " keys.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to clear Redis: " + e.getMessage());
        }
    }
}