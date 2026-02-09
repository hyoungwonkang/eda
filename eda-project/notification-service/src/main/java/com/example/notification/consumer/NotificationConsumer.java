package com.example.notification.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.example.common.dto.event.OrderCreatedEvent; 

@Slf4j
@Component
public class NotificationConsumer {

  @SqsListener("notification-events")  // 테라폼에서 만든 큐 이름
  public void sendNotification(OrderCreatedEvent event) {
    log.info("[Notification Service] Received Order Event: {}", event.orderId());
    
    // 실제 문자/이메일 발송 로직 필요
    sendSms(event.orderId());
    sendEmail(event.orderId());
  }

  private void sendSms(String orderId) {
    log.info("Sending SMS to user... [Order ID: {}] -> '주문이 접수되었습니다.'", orderId);
  }

  private void sendEmail(String orderId) {
    log.info("Sending Email to user... [Order ID: {}] -> '주문 상세 내역입니다.'", orderId);
  }
}