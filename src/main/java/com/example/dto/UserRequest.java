package com.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Данные для создания или обновления пользователя")
public class UserRequest {

    @Schema(
            description = "Имя пользователя",
            example = "Anna",
            requiredMode = Schema.RequiredMode.REQUIRED)

    @NotBlank
    @Size(max = 100)
    private String name;

    @Schema(
            description = "Email пользователя. Должен быть уникальным",
            example = "anna@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    @Schema(
            description = "Возраст пользователя",
            example = "25",
            minimum = "1",
            maximum = "120")

    @Min(1)
    @Max(120)
    private Integer age;

    public UserRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}