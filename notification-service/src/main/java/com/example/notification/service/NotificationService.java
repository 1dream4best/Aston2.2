package com.example.notification.service;

import com.example.notification.messaging.UserEvent;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final JavaMailSender mailSender;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendUserNotification(UserEvent event) {

        String subject = "Уведомление об аккаунте";

        String text;

        if ("CREATED".equals(event.operation())) {
            text = "Здравствуйте! Ваш аккаунт на сайте <<Кхъ>> был успешно создан.";
        } else if ("DELETED".equals(event.operation())) {
            text = "Здравствуйте! Ваш аккаунт был удалён.";
        } else {
            throw new IllegalArgumentException(
                    "Неизвестная операция: " + event.operation());
        }

        sendEmail(event.email(), subject, text);
    }

    public void sendEmail(String email, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}