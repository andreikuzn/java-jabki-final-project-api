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

@Tag(name = "Аутентификация", description = "Регистрация и получение JWT-токена")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создаёт нового пользователя с выбранной ролью (user/admin). Админ может быть только один.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован"),
                    @ApiResponse(responseCode = "400", description = "Ошибка регистрации")
            }
    )

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request.getRole() == bookShop.model.Role.ADMIN && authService.adminExists()) {
            return ResponseEntity.badRequest().body("Admin already exists");
        }
        authService.register(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @Operation(
            summary = "Вход пользователя",
            description = "Возвращает JWT-токен для доступа к защищённым ресурсам.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешная аутентификация"),
                    @ApiResponse(responseCode = "401", description = "Неверные учетные данные")
            }
    )

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(authResponse);
    }
}
