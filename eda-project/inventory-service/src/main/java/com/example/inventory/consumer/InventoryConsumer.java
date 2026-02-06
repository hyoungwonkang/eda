package com.example.inventory.consumer;

import java.util.function.Consumer;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.common.dto.event.InventoryFailedEvent;
import com.example.common.dto.event.InventoryReservedEvent;
import com.example.common.dto.event.OrderCreatedEvent;
import com.example.inventory.service.InventoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class InventoryConsumer {
    
    private final InventoryService inventoryService;
    private final StreamBridge streamBridge;

    @Bean
    Consumer<OrderCreatedEvent> processInventory() {
        return event -> {
            log.info("재고 서비스 - 재고품 번호: {}", event.productId());
 
            // 재고 차감 로직
            try {
                // 재고 차감 시도
                inventoryService.decreaseStock(event.productId(), event.quantity());
                InventoryReservedEvent successEvent = new InventoryReservedEvent(event.orderId());
                streamBridge.send("inventory-reserved-out-0", successEvent);
                log.info("Stock Reserved!");
            } catch (Exception e) {
                // 재고 차감 실패
                InventoryFailedEvent failedEvent = new InventoryFailedEvent(event.orderId(), e.getMessage());
                streamBridge.send("inventory-failed-out-0", failedEvent);
                log.info("Stock Reservation Failed!", e.getMessage());
            }
        };
    }
}
