package com.example.inventory.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.example.common.dto.event.InventoryFailedEvent;
import com.example.common.dto.event.InventoryReservedEvent;
import com.example.common.dto.event.OrderCreatedEvent;
import com.example.inventory.service.InventoryService;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryConsumer {

  // order-events 큐 메시지 읽기 -> 성공 시 inventory-reserved-events, 실패 시 inventory-failed-events 큐로 메시지 보내기

  private final InventoryService inventoryService;
  
  private final SqsTemplate sqsTemplate;  // 메시지 전송용

  @SqsListener("order-events")  // 메시지를 읽을 큐 이름 명시
  public void processInventory(OrderCreatedEvent event) {
    log.info("Received Order Event from SQS: ID={}, Product={}", event.orderId(), event.productId());

    try {
      // 재고 차감 로직 실행
      inventoryService.decreaseStock(event.productId(), event.quantity());

      // 성공 이벤트 발행 (SqsTemplate 사용)
      InventoryReservedEvent successEvent = new InventoryReservedEvent(event.orderId());
      sqsTemplate.send(to -> to
        .queue("inventory-reserved-events")  // 성공 큐 이름
        .payload(successEvent)
      );
      log.info("Sent Success Event to SQS");

    } catch (Exception e) {
      log.error("Stock Deduction Failed: {}", e.getMessage());

      // 실패 이벤트 발행 (SqsTemplate 사용)
      InventoryFailedEvent failedEvent = new InventoryFailedEvent(event.orderId(), e.getMessage());
      sqsTemplate.send(to -> to
        .queue("inventory-failed-events")  // 실패 큐 이름
        .payload(failedEvent)
      );
    }
  }
}