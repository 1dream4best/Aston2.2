package com.example.controller;

import com.example.assembler.UserModelAssembler;
import com.example.dto.UserRequest;
import com.example.dto.UserResponse;
import com.example.service.UserService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "API для управления пользователями")
public class UserController {

    private final UserService userService;
    private final UserModelAssembler assembler;

    public UserController(UserService userService,
                          UserModelAssembler assembler) {
        this.userService = userService;
        this.assembler = assembler;
    }

    @GetMapping
    @Operation(
            summary = "Получить всех пользователей",
            description = "Возвращает список пользователей с HATEOAS-ссылками.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Список пользователей успешно получен")
    })
    public CollectionModel<EntityModel<UserResponse>> getAllUsers() {
        List<EntityModel<UserResponse>> users = userService.getAllUsers()
                .stream()
                .map(assembler::toModel)
                .toList();

        return CollectionModel.of(
                users,
                org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
                        .linkTo(org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
                                .methodOn(UserController.class).getAllUsers())
                        .withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить пользователя по ID",
            description = "Находит пользователя по идентификатору и возвращает его с HATEOAS-ссылками.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь найден"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public EntityModel<UserResponse> getUserById(
            @Parameter(description = "Идентификатор пользователя", example = "1")
            @PathVariable Long id) {
        return assembler.toModel(userService.getUserById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Создать пользователя",
            description = "Создаёт нового пользователя. Email должен быть уникальным.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Пользователь успешно создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных"),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует")
    })
    public EntityModel<UserResponse> createUser(
            @Valid @RequestBody UserRequest request) {
        return assembler.toModel(userService.createUser(request));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Обновить пользователя",
            description = "Обновляет данные пользователя по ID. Email должен быть уникальным.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлён"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует")
    })
    public EntityModel<UserResponse> updateUser(
            @Parameter(description = "Идентификатор пользователя", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        return assembler.toModel(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Удалить пользователя",
            description = "Удаляет пользователя по ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Пользователь успешно удалён"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public void deleteUser(
            @Parameter(description = "Идентификатор пользователя", example = "1")
            @PathVariable Long id) {
        userService.deleteUser(id);
    }
}