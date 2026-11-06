package com.example.notification.messaging;

import com.example.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    public UserEventConsumer(
            ObjectMapper objectMapper,
            NotificationService notificationService) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
    }

    @KafkaListener(
            topics = "${app.kafka.topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String message) {
        try {
            UserEvent event = objectMapper.readValue(
                    message, UserEvent.class);

            System.out.println("Получено событие Kafka:");
            System.out.println("Операция: " + event.operation());
            System.out.println("Email: " + event.email());

            notificationService.sendUserNotification(event);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Не удалось обработать Kafka-событие", e);
        }
    }
}