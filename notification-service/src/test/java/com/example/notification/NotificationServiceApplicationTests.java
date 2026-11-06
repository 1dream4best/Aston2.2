
package com.example.notification.service;

import com.example.notification.messaging.UserEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(mailSender);
    }

    @Test
    void shouldSendCreatedNotification() {
        UserEvent event = new UserEvent(
                "CREATED",
                "test@example.com");

        notificationService.sendUserNotification(event);

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();

        assertEquals("test@example.com", message.getTo()[0]);
        assertEquals("Уведомление об аккаунте", message.getSubject());
        assertTrue(message.getText().contains("успешно создан"));
    }

    @Test
    void shouldSendDeletedNotification() {
        UserEvent event = new UserEvent(
                "DELETED",
                "test@example.com");

        notificationService.sendUserNotification(event);

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();

        assertEquals("test@example.com", message.getTo()[0]);
        assertEquals("Уведомление об аккаунте", message.getSubject());
        assertTrue(message.getText().contains("удалён"));
    }

    @Test
    void shouldThrowExceptionForUnknownOperation() {
        UserEvent event = new UserEvent(
                "UNKNOWN",
                "test@example.com");

        assertThrows(
                IllegalArgumentException.class,
                () -> notificationService.sendUserNotification(event));

        verifyNoInteractions(mailSender);
    }
}
