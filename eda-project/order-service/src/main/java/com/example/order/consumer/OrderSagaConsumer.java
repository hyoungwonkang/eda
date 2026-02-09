package com.example.order.consumer;

import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.common.dto.event.InventoryFailedEvent;
import com.example.common.dto.event.InventoryReservedEvent;
import com.example.order.service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OrderSagaConsumer {
    
    private final OrderService orderService;

    @Bean
    Consumer<InventoryReservedEvent> handleInventorySuccess() {
        return event -> {
            log.info("주문 서비스 - 재고 차감 성공 - Order ID: {}", event.orderId());
            orderService.completeOrder(event.orderId());
        };
    }

    @Bean
    Consumer<InventoryFailedEvent> handleInventoryFailure() {
        return event -> {
            log.info("주문 서비스 - 재고 차감 실패 - Order ID: {}, 실패이유: {}", event.orderId(), event.reason());
            orderService.cancelOrder(event.orderId());
        };
    }
}
