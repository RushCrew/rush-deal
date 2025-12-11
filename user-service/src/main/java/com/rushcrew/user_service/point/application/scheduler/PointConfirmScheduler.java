package com.rushcrew.user_service.point.application.scheduler;

import com.rushcrew.user_service.point.application.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointConfirmScheduler {

    private final PointService pointService;

    // 매일 1분마다 실행
    @Scheduled(cron = "0 * * * * *")
    public void confirmExpiredPendingPoints() {
        log.info("포인트 확정 스케줄러 시작");
        pointService.confirmExpiredPendingPoints();
        log.info("포인트 확정 스케줄러 종료");
    }
}
