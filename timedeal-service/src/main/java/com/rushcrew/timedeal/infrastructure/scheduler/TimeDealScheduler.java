package com.rushcrew.timedeal.infrastructure.scheduler;

import com.rushcrew.timedeal.application.service.TimeDealService;
import com.rushcrew.timedeal.domain.port.TimeDealCache;
import com.rushcrew.timedeal.domain.port.TimeDealQueueKey;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TimeDealScheduler {

    private final TimeDealCache timeDealCache;
    private final TimeDealService timeDealService;

    @Scheduled(fixedDelay = 1000)
    public void startScheduler() {
        List<String> startedTimeDealIds = timeDealCache.getTimeDealIds(TimeDealQueueKey.START);
        if (startedTimeDealIds.isEmpty()) {
            return;
        }

        timeDealService.startTimeDeals(startedTimeDealIds);
    }

    @Scheduled(fixedDelay = 1000)
    public void endScheduler() {
        List<String> endedTimeDealIds = timeDealCache.getTimeDealIds(TimeDealQueueKey.END);
        if (endedTimeDealIds.isEmpty()) {
            return;
        }

        timeDealService.endTimeDeals(endedTimeDealIds);
    }
}
