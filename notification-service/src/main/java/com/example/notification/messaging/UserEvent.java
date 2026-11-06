package com.example.notification.messaging;

public record UserEvent(
        String operation,
        String email
) {
}