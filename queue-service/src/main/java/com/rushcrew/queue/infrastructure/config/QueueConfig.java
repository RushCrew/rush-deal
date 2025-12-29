package com.rushcrew.queue.infrastructure.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class QueueConfig {

    // 대기열 스케줄러 전용 스레드 풀 정의
    @Bean(name = "queueSchedulerExecutor")
    public Executor queueSchedulerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 기본적으로 10개의 스레드를 항상 유지
        executor.setCorePoolSize(10);
        // 바쁠 때 최대 20개까지 확장 (기본 10개 스레드도 점유되었고, 대기열 용량도 꽉 찼을 경우)
        executor.setMaxPoolSize(20);
        // 대기열 용량 (작업이 밀리면 500개까지 쌓아둠)
        executor.setQueueCapacity(500);
        // 스레드 이름 접두사 (디버깅용)
        executor.setThreadNamePrefix("queue-scheduler-");

        executor.initialize();
        return executor;
    }
}
