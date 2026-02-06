package com.example.common.dto.event;

import java.time.LocalDateTime;

public record OrderCreatedEvent(
    String productId,
    String orderId,
    Integer quantity,
    LocalDateTime occuredAt
) {
    
}
