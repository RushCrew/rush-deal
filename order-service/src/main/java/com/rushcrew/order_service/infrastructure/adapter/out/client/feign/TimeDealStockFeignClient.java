package com.rushcrew.order_service.infrastructure.adapter.out.client.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.rushcrew.order_service.infrastructure.dto.timedeal.TimeDealResponse;
import com.rushcrew.order_service.infrastructure.dto.timedeal.TimeDealStockResponse;

@FeignClient(name = "timedeal-service", url = "${TIMEDEAL_SERVICE_URL}")
public interface TimeDealStockFeignClient {

	@GetMapping("/api/v1/timedeals/{timeDealId}/order")
	TimeDealResponse getTimeDeal(@PathVariable("timeDealId") String timeDealId);

	@GetMapping("/api/v1/stocks/{timeDealStockId}")
	TimeDealStockResponse getTimeDealStockDetail(@PathVariable("timeDealStockId") String timeDealStockId);
}
