package com.example.messaging;

public record UserEvent(
        UserEventType operation,
        String email) {
}