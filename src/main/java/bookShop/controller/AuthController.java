package bookShop.controller;

import bookShop.model.RegisterRequest;
import bookShop.util.ApiResponse;
import bookShop.model.request.AuthRequest;
import bookShop.model.response.AuthResponse;
import bookShop.model.response.UserResponse;
import bookShop.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@Tag(name = "Аутентификация", description = "Регистрация и получение токена")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(
            summary = "Регистрация пользователя",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Пользователь успешно зарегистрирован",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": null, \"message\": \"Пользователь успешно зарегистрирован\", \"status\": 200, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": { \"id\": 123, \"username\": \"vasya\", \"role\": \"USER\", \"phone\": \"79991234567\", \"email\": \"vasya@mail.com\", \"loyaltyPoints\": 0, \"loyaltyLevel\": \"Новичок\", \"activeLoans\": [] } }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Ошибка валидации или пользователь уже существует",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "Ошибка валидации",
                                                    value = "{ \"error\": \"VALIDATION_ERROR\", \"message\": \"Пароль должен быть от 6 до 64 символов\", \"status\": 400, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
                                            ),
                                            @ExampleObject(
                                                    name = "Пользователь уже существует",
                                                    value = "{ \"error\": \"USER_ALREADY_EXISTS\", \"message\": \"Пользователь с таким именем уже существует\", \"status\": 400, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
                                            )
                                    }
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "500",
                            description = "Внутренняя ошибка сервера",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"INTERNAL_ERROR\", \"message\": \"Произошла внутренняя ошибка сервера\", \"status\": 500, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
                                    )
                            )
                    )
            }
    )
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse userResponse = authService.register(request);
        return ResponseEntity.ok(ApiResponse.successWithData(
                userResponse, "Пользователь успешно зарегистрирован"
        ));
    }

    @Operation(
            summary = "Вход пользователя",
            description = "Возвращает токен для доступа к защищённым ресурсам.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Успешная аутентификация",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": null, \"message\": null, \"status\": 200, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": { \"token\": \"eyJhb...\", \"userId\": 123, \"role\": \"USER\" } }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Неверный логин или пароль",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"INVALID_CREDENTIALS\", \"message\": \"Неверный логин или пароль\", \"status\": 400, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Пользователь не найден",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"USER_NOT_FOUND\", \"message\": \"Пользователь не найден\", \"status\": 404, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "500",
                            description = "Внутренняя ошибка сервера",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"INTERNAL_ERROR\", \"message\": \"Произошла внутренняя ошибка сервера\", \"status\": 500, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
                                    )
                            )
                    )
            }
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.successWithData(authResponse, null));
    }
}