package com.rushcrew.user_service.point.infrastructure.config;

import com.rushcrew.user_service.point.domain.repository.PointHistoryQueryRepository;
import com.rushcrew.user_service.point.domain.service.PointDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public PointDomainService pointDomainService(PointHistoryQueryRepository pointHistoryQueryRepository) {
        return new PointDomainService(pointHistoryQueryRepository);
    }
}
