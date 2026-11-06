package com.example.notification.controller;

import com.example.notification.dto.EmailRequest;
import com.example.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/email")
    public ResponseEntity<String> sendEmail(
            @Valid @RequestBody EmailRequest request) {

        notificationService.sendEmail(
                request.email(),
                request.subject(),
                request.text());

        return ResponseEntity.ok("Письмо успешно отправлено");
    }
}