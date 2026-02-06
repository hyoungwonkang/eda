package com.example.common.dto.event;

public record OrderFailureEvent(
    String orderId,
    String reason
) {
}