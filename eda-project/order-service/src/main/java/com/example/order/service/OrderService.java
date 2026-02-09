package com.example.order.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.common.dto.OrderCreatedRequest;
import com.example.common.dto.event.OrderCreatedEvent;
import com.example.order.client.InventoryClient;
import com.example.order.entity.Order;
import com.example.order.entity.OrderStatus;
import com.example.order.repository.OrderRepository;

import io.awspring.cloud.sns.core.SnsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

  // JPA Repository
  private final OrderRepository orderRepository;

  // SnsTemplate: 1:N 통신 위해 SNS로 발행
  private final SnsTemplate snsTemplate;

  // InventoryClient: inventory-service와 동기 통신
  private final InventoryClient inventoryClient;

  @Transactional
  public String createOrder(OrderCreatedRequest request) {
    // 가장 먼저 재고부터 확인
    Integer currentStock = inventoryClient.getStock(request.productId());
    if (currentStock < request.quantity()) {
      throw new RuntimeException("Order Fail: Out of stock");  // 컨트롤러로 예외 던짐
    }

    // 주문 아이디 생성
    String orderId = UUID.randomUUID().toString();

    // 주문 저장 로직 (DB에 PENDING 상태로 저장)
    Order order = Order.builder()
      .orderId(orderId)
      .productId(request.productId())
      .quantity(request.quantity())
      .status(OrderStatus.PENDING)
      .build();
    orderRepository.save(order);

    // 주문 생성 이벤트 객체 생성
    OrderCreatedEvent event = new OrderCreatedEvent(
      orderId,
      request.productId(),
      request.quantity(),
      LocalDateTime.now()
    );
    
    // 주문 생성 이벤트 발행 (SNS Topic으로 내보내면 이를 구독 중인 모든 SQS가 메시지를 가져감)
    snsTemplate.sendNotification("order-events-topic", event, "Order Created");
    log.info("Sent Event to SNS: {}", event);

    return orderId;
  }

  @Transactional
  public void completeOrder(String orderId) {
    Order foundOrder = orderRepository.findById(orderId)
      .orElseThrow(() -> new RuntimeException("Order not found"));
    foundOrder.complete(); // Dirty Checking
  }

  @Transactional
  public void cancelOrder(String orderId) {
    Order foundOrder = orderRepository.findById(orderId)
      .orElseThrow(() -> new RuntimeException("Order not found"));
    foundOrder.cancel(); // Dirty Checking
  }
}
