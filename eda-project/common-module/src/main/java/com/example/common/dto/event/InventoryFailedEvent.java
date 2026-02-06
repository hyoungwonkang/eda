package com.example.common.dto.event;

public record InventoryFailedEvent(
    String orderId,
    String reason
) {
}
