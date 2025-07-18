package bookShop.controller;

import bookShop.model.RegisterRequest;
import bookShop.model.AuthRequest;
import bookShop.model.AuthResponse;
import bookShop.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import javax.validation.Valid;

@Tag(name = "Аутентификация", description = "Регистрация и получение токена")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создаёт нового пользователя с выбранной ролью (user/admin). Пользователей с ролью ADMIN может быть не более 3.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован"),
                    @ApiResponse(responseCode = "400", description = "Ошибка регистрации")
            }
    )
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            return ResponseEntity.ok("User registered successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Вход пользователя",
            description = "Возвращает токен для доступа к защищённым ресурсам.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешная аутентификация"),
                    @ApiResponse(responseCode = "400", description = "Неверный логин или пароль")
            }
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(authResponse);
    }
}