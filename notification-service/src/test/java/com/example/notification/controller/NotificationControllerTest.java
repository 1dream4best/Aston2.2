
package com.example.notification.controller;

import com.example.notification.dto.EmailRequest;
import com.example.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class NotificationControllerTest {

    @Test
    void shouldSendEmailAndReturnSuccess() {
        NotificationService notificationService =
                mock(NotificationService.class);

        NotificationController controller =
                new NotificationController(notificationService);

        EmailRequest request = new EmailRequest(
                "test@example.com",
                "Тест",
                "Проверка отправки письма");

        ResponseEntity<String> response = controller.sendEmail(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Письмо успешно отправлено", response.getBody());

        verify(notificationService).sendEmail(
                "test@example.com",
                "Тест",
                "Проверка отправки письма");
    }
}
