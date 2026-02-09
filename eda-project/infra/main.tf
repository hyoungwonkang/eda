// AWS 프로바이더 설정
provider "aws" {
  region = "ap-northeast-2"
}

# SNS Topic 생성
resource "aws_sns_topic" "order_events_topic" {
  name = "order-events-topic"
}

# SQS 대기열 생성1 (주문 생성)
resource "aws_sqs_queue" "order_events_queue" {
  name                      = "order-events"
  message_retention_seconds = 86400
  receive_wait_time_seconds = 20
}

# SQS 대기열 생성2 (재고 확보 성공)
resource "aws_sqs_queue" "inventory_reserved_queue" {
  name                      = "inventory-reserved-events"
  message_retention_seconds = 86400
  receive_wait_time_seconds = 20
}

# SQS 대기열 생성3 (재고 확보 실패)
resource "aws_sqs_queue" "inventory_failed_queue" {
  name                      = "inventory-failed-events"
  message_retention_seconds = 86400
  receive_wait_time_seconds = 20
}

# SQS 대기열 생성4 (알림 전용 큐)
resource "aws_sqs_queue" "notification_events_queue" {
  name                      = "notification-events"
  message_retention_seconds = 86400
  receive_wait_time_seconds = 20
}

# 구독할 큐들의 목록을 정의 (Map 구조)
locals {
  # "이름" = "큐의 ARN"
  order_subscribers = {
    inventory    = aws_sqs_queue.order_events_queue.arn
    notification = aws_sqs_queue.notification_events_queue.arn
  }
}

# for_each문으로 SNS -> SQS 구독처리 (inventory, notification)
resource "aws_sns_topic_subscription" "order_events_fanout" {
  for_each = local.order_subscribers  # 위에서 정의한 목록만큼 반복
  
  topic_arn = aws_sns_topic.order_events_topic.arn
  protocol  = "sqs"
  endpoint  = each.value              # 목록의 값 (Queue ARN)이 들어감
  raw_message_delivery = true         # 원시 데이터 전송 활성화 (AWS가 데이터를 별도 JSON으로 묶는 것 방지)
}

# [참고] 정책은 큐마다 달라질 가능성이 있으므로 합치지 않고 따로 작성

# SQS 정책1 (SNS가 Inventory SQS에 쓸 수 있게 허용)
resource "aws_sqs_queue_policy" "order_events_policy" {
  queue_url = aws_sqs_queue.order_events_queue.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "sns.amazonaws.com"
        }
        Action   = "sqs:SendMessage"
        Resource = aws_sqs_queue.order_events_queue.arn
        Condition = {
          ArnEquals = {
            "aws:SourceArn" = aws_sns_topic.order_events_topic.arn
          }
        }
      }
    ]
  })
}

# SQS 정책2 (SNS가 Notificatioin SQS에 쓸 수 있게 허용)
resource "aws_sqs_queue_policy" "notification_events_policy" {
  queue_url = aws_sqs_queue.notification_events_queue.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "sns.amazonaws.com"
        }
        Action   = "sqs:SendMessage"
        Resource = aws_sqs_queue.notification_events_queue.arn
        Condition = {
          ArnEquals = {
            "aws:SourceArn" = aws_sns_topic.order_events_topic.arn
          }
        }
      }
    ]
  })
}


// 결과 출력
output "sns_topic_arn" {
  value = aws_sns_topic.order_events_topic.arn
}

output "sqs_queue_url" {
  value = aws_sqs_queue.order_events_queue.id
}

output "inventory_reserved_queue_url" {
  value = aws_sqs_queue.inventory_reserved_queue.id
}

output "inventory_failed_queue_url" {
  value = aws_sqs_queue.inventory_failed_queue.id
}

output "notification_queue_url" {
  value = aws_sqs_queue.notification_events_queue.id
}