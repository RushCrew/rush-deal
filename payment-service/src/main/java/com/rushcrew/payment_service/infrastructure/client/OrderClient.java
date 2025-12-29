package com.rushcrew.payment_service.infrastructure.client;


import com.rushcrew.payment_service.global.security.model.UserDetailsImpl;
import com.rushcrew.payment_service.infrastructure.client.dto.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "order-service", url = "${ORDER_SERVICE_URL}")
public interface OrderClient {

    @GetMapping("/api/v1/orders/{orderId}")
    OrderResponse getOrder(@PathVariable("orderId") UUID orderId);
}
