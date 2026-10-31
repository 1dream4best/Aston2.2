package com.example.controller;

import com.example.dto.UserRequest;
import com.example.dto.UserResponse;
import com.example.exception.GlobalExceptionHandler;
import com.example.exception.UserNotFoundException;
import com.example.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private UserResponse sampleUser() {
        return new UserResponse(
                1L,
                "Anna",
                "anna@example.com",
                25,
                LocalDateTime.of(2025, 1, 1, 12, 0));
    }

    @Test
    void getAllUsersShouldReturnList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(sampleUser()));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Anna"))
                .andExpect(jsonPath("$[0].email").value("anna@example.com"));
    }

    @Test
    void getUserByIdShouldReturnUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(sampleUser());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Anna"));
    }

    @Test
    void createUserShouldReturnCreated() throws Exception {
        when(userService.createUser(any(UserRequest.class))).thenReturn(sampleUser());

        String requestBody = """
                {
                  "name": "Anna",
                  "email": "anna@example.com",
                  "age": 25
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("anna@example.com"));
    }

    @Test
    void updateUserShouldReturnUpdatedUser() throws Exception {
        when(userService.updateUser(eq(1L), any(UserRequest.class)))
                .thenReturn(sampleUser());

        String requestBody = """
                {
                  "name": "Anna",
                  "email": "anna@example.com",
                  "age": 25
                }
                """;

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Anna"));
    }

    @Test
    void deleteUserShouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void createUserWithInvalidEmailShouldReturnBadRequest() throws Exception {
        String requestBody = """
                {
                  "name": "Anna",
                  "email": "not-an-email",
                  "age": 25
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getMissingUserShouldReturnNotFound() throws Exception {
        when(userService.getUserById(99L))
                .thenThrow(new UserNotFoundException("User not found with id: 99"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found with id: 99"));
    }
}