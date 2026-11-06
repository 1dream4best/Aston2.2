package com.example.notification.messaging;

import com.example.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserEventConsumerTest {

    private NotificationService notificationService;
    private UserEventConsumer consumer;

    @BeforeEach
    void setUp() {
        notificationService = mock(NotificationService.class);

        consumer = new UserEventConsumer(
                new ObjectMapper(),
                notificationService);
    }

    @Test
    void shouldProcessCreatedEvent() {
        String message = """
                {"operation":"CREATED","email":"test@example.com"}
                """;

        consumer.consume(message);

        verify(notificationService).sendUserNotification(
                new UserEvent("CREATED", "test@example.com"));
    }

    @Test
    void shouldProcessDeletedEvent() {
        String message = """
                {"operation":"DELETED","email":"test@example.com"}
                """;

        consumer.consume(message);

        verify(notificationService).sendUserNotification(
                new UserEvent("DELETED", "test@example.com"));
    }

    @Test
    void shouldThrowExceptionForInvalidJson() {
        String message = "это не JSON";

        assertThrows(
                IllegalStateException.class,
                () -> consumer.consume(message));

        verifyNoInteractions(notificationService);
    }
}
