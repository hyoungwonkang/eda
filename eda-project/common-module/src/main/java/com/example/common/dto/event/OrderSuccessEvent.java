package com.example.common.dto.event;

public record OrderSuccessEvent(
    String orderId,
    String productId,
    int quantity
) {
    
}
