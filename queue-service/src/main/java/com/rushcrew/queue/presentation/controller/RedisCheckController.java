package com.rushcrew.queue.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
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
}