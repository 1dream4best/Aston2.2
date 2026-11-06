package com.example.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailRequest(

        @NotBlank(message = "Email не должен быть пустым")
        @Email(message = "Некорректный email")
        String email,

        @NotBlank(message = "Тема письма обязательна")
        String subject,

        @NotBlank(message = "Текст письма обязателен")
        String text) {
}