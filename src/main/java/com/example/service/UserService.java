package com.example.service;

import com.example.dao.UserRepository;
import com.example.dto.UserRequest;
import com.example.dto.UserResponse;
import com.example.entity.User;
import com.example.exception.UserNotFoundException;
import com.example.messaging.UserEvent;
import com.example.messaging.UserEventPublisher;
import com.example.messaging.UserEventType;
import com.example.exception.DuplicateEmailException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserEventPublisher userEventPublisher;

    public UserService(
            UserRepository userRepository,
            UserEventPublisher userEventPublisher) {
        this.userRepository = userRepository;
        this.userEventPublisher = userEventPublisher;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("Пользователь с ID " + id + " не найден."));

        return mapToResponse(user);
    }

    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(
                    "Пользователь с такой почтой уже существует.");
        }

        User user = new User(
                request.getName(),
                request.getEmail(),
                request.getAge());

        User savedUser = userRepository.save(user);

        userEventPublisher.publish(
                new UserEvent(UserEventType.CREATED, savedUser.getEmail()));

        return mapToResponse(savedUser);
    }

    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("Пользователь с ID " + id + " не найден."));

        userRepository.findByEmail(request.getEmail())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateEmailException(
                            "Пользователь с такой почтой уже существует.");
                });

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setAge(request.getAge());

        User updatedUser = userRepository.save(user);

        return mapToResponse(updatedUser);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("Пользователь с ID " + id + " не найден."));

        userRepository.delete(user);

        userEventPublisher.publish(
                new UserEvent(UserEventType.DELETED, user.getEmail()));
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge(),
                user.getCreatedAt());
    }
}