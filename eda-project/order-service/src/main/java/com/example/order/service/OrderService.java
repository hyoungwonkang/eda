package com.example.order.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import org.springframework.transaction.annotation.Transactional;

import com.example.common.dto.OrderCreatedRequest;
import com.example.common.dto.event.OrderCreatedEvent;
import com.example.order.entity.Order;
import com.example.order.entity.OrderStatus;
import com.example.order.repository.OrderRespository;

import org.springframework.cloud.stream.function.StreamBridge;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    // 이벤트 메시지 발행 도구 StreamBridge 주입
    private final StreamBridge streamBridge;

    // DB 접근을 위한 OrderRepository 주입
    private final OrderRespository orderRespository;

    @Transactional
    public String createOrder(OrderCreatedRequest request) {
        // 주문 ID
        String orderId = UUID.randomUUID().toString();

        // DB 주문 저장 필요
        Order newOrder = Order.builder()
            .orderId(orderId)
            .productId(request.productId())
            .quantity(request.quantity())
            .orderStatus(OrderStatus.PENDING)
            .build();

        orderRespository.save(newOrder);

        // 주문 생성 이벤트 메시지 생성
        OrderCreatedEvent event = new OrderCreatedEvent(
            request.productId(),
            orderId,
            request.quantity(),
            LocalDateTime.now());
        streamBridge.send("orderCreated-out-0", event);

        // 반환
        return orderId;
    }

    public void completeOrder(String orderId) {
        Order foundOrder = orderRespository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        foundOrder.complete();
    }

    public void cancelOrder(String orderId) {
        Order foundOrder = orderRespository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        foundOrder.cancel();
    }
}
