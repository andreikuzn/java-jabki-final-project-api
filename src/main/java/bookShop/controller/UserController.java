package bookShop.controller;

import bookShop.model.RegisterRequest;
import bookShop.model.UserResponse;
import bookShop.model.AppUserDetails;
import bookShop.model.ApiResponse;
import bookShop.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@Tag(name = "Пользователи", description = "Управление пользователями")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Список всех пользователей",
            description = "Только для администратора",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Список пользователей",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = """
{
  "error": null,
  "message": null,
  "status": 200,
  "timestamp": "2025-07-21T13:00:00.000",
  "data": [
    { ... }
  ]
}
"""
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Пользователи не найдены",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"USER_NOT_FOUND\", \"message\": \"Пользователи не найдены\", \"status\": 404, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Пользователь не авторизован",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"USER_NOT_AUTHENTICATED\", \"message\": \"Пользователь не авторизован в системе\", \"status\": 401, \"timestamp\": \"2025-07-21T14:10:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Доступ запрещён: недостаточно прав",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"FORBIDDEN\", \"message\": \"Действие запрещено: недостаточно прав\", \"status\": 403, \"timestamp\": \"2025-07-21T14:10:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "500",
                            description = "Внутренняя ошибка сервера",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"INTERNAL_ERROR\", \"message\": \"Произошла внутренняя ошибка сервера\", \"status\": 500, \"timestamp\": \"2025-07-21T14:10:00.000\", \"data\": null }"
                                    )
                            )
                    )
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse> getAllUsers() {
        List<UserResponse> data = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.successWithData(data, null));
    }

    @Operation(
            summary = "Профиль текущего пользователя",
            description = "Информация о текущем пользователе",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Профиль пользователя",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = """
{
  "error": null,
  "message": null,
  "status": 200,
  "timestamp": "2025-07-21T13:00:00.000",
  "data": [
    { ... }
  ]
}
"""
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Пользователь не авторизован",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"USER_NOT_AUTHENTICATED\", \"message\": \"Пользователь не авторизован в системе\", \"status\": 401, \"timestamp\": \"2025-07-21T14:10:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "500",
                            description = "Внутренняя ошибка сервера",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"INTERNAL_ERROR\", \"message\": \"Произошла внутренняя ошибка сервера\", \"status\": 500, \"timestamp\": \"2025-07-21T14:10:00.000\", \"data\": null }"
                                    )
                            )
                    )
            }
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getMe(Authentication authentication) {
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        UserResponse data = userService.getUserResponseById(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.successWithData(data, null));
    }

    @Operation(
            summary = "Обновить пользователя",
            description = "Обновить данные пользователя (password, phone, email могут менять себе все, role - только админ, username нельзя)",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Пользователь успешно обновлён",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = """
{
  "error": null,
  "message": "Пользователь успешно обновлён",
  "status": 200,
  "timestamp": "2025-07-21T13:00:00.000",
  "data": [
    { ... }
  ]
}
"""
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Ошибка валидации",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"VALIDATION_ERROR\", \"message\": \"Некорректные данные\", \"status\": 400, \"timestamp\": \"2025-07-21T14:10:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Пользователь не авторизован",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"USER_NOT_AUTHENTICATED\", \"message\": \"Пользователь не авторизован в системе\", \"status\": 401, \"timestamp\": \"2025-07-21T14:10:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Доступ запрещён: недостаточно прав",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"FORBIDDEN\", \"message\": \"Действие запрещено: недостаточно прав\", \"status\": 403, \"timestamp\": \"2025-07-21T14:10:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Пользователь не найден",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"USER_NOT_FOUND\", \"message\": \"Пользователь не найден\", \"status\": 404, \"timestamp\": \"2025-07-21T14:10:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "500",
                            description = "Внутренняя ошибка сервера",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"INTERNAL_ERROR\", \"message\": \"Произошла внутренняя ошибка сервера\", \"status\": 500, \"timestamp\": \"2025-07-21T14:10:00.000\", \"data\": null }"
                                    )
                            )
                    )
            }
    )
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateUser(@PathVariable Long id,
                                                  @Valid @RequestBody RegisterRequest request,
                                                  Authentication authentication) {
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        UserResponse data = userService.updateUser(id, request, userDetails);
        return ResponseEntity.ok(ApiResponse.successWithData(data, "Пользователь успешно обновлён"));
    }

    @Operation(
            summary = "Создать пользователя",
            description = "Создать нового пользователя (только для администратора)",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Пользователь успешно создан",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = """
{
  "error": null,
  "message": "Пользователь успешно создан",
  "status": 200,
  "timestamp": "2025-07-21T13:00:00.000",
  "data": [
    { ... }
  ]
}
"""
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Ошибка валидации",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"VALIDATION_ERROR\", \"message\": \"Некорректные данные\", \"status\": 400, \"timestamp\": \"2025-07-21T14:10:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Пользователь не авторизован",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"USER_NOT_AUTHENTICATED\", \"message\": \"Пользователь не авторизован в системе\", \"status\": 401, \"timestamp\": \"2025-07-21T14:10:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Доступ запрещён: недостаточно прав",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"FORBIDDEN\", \"message\": \"Действие запрещено: недостаточно прав\", \"status\": 403, \"timestamp\": \"2025-07-21T14:10:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "500",
                            description = "Внутренняя ошибка сервера",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"INTERNAL_ERROR\", \"message\": \"Произошла внутренняя ошибка сервера\", \"status\": 500, \"timestamp\": \"2025-07-21T14:10:00.000\", \"data\": null }"
                                    )
                            )
                    )
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse> createUser(@Valid @RequestBody RegisterRequest request) {
        UserResponse data = userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.successWithData(data, "Пользователь успешно создан"));
    }

    @Operation(
            summary = "Удалить пользователя по id",
            description = "Только для администратора",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Пользователь удалён",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = """
{
  "error": null,
  "message": "Пользователь удалён",
  "status": 200,
  "timestamp": "2025-07-21T13:00:00.000",
  "data": [
    { ... }
  ]
}
"""
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Пользователь не авторизован",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"USER_NOT_AUTHENTICATED\", \"message\": \"Пользователь не авторизован в системе\", \"status\": 401, \"timestamp\": \"2025-07-21T14:10:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Доступ запрещён: недостаточно прав",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"FORBIDDEN\", \"message\": \"Действие запрещено: недостаточно прав\", \"status\": 403, \"timestamp\": \"2025-07-21T14:10:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Пользователь не найден",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"USER_NOT_FOUND\", \"message\": \"Пользователь не найден\", \"status\": 404, \"timestamp\": \"2025-07-21T14:10:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "500",
                            description = "Внутренняя ошибка сервера",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"INTERNAL_ERROR\", \"message\": \"Произошла внутренняя ошибка сервера\", \"status\": 500, \"timestamp\": \"2025-07-21T14:10:00.000\", \"data\": null }"
                                    )
                            )
                    )
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.successWithData(null, "Пользователь удалён"));
    }

    @Operation(
            summary = "Получить пользователя по id",
            description = "Только для администратора",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Пользователь найден",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = """
{
  "error": null,
  "message": null,
  "status": 200,
  "timestamp": "2025-07-21T13:00:00.000",
  "data": [
    { ... }
  ]
}
"""
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Пользователь не найден",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"USER_NOT_FOUND\", \"message\": \"Пользователь не найден\", \"status\": 404, \"timestamp\": \"2025-07-21T14:10:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Пользователь не авторизован",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"USER_NOT_AUTHENTICATED\", \"message\": \"Пользователь не авторизован в системе\", \"status\": 401, \"timestamp\": \"2025-07-21T14:10:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Доступ запрещён: недостаточно прав",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"FORBIDDEN\", \"message\": \"Действие запрещено: недостаточно прав\", \"status\": 403, \"timestamp\": \"2025-07-21T14:10:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "500",
                            description = "Внутренняя ошибка сервера",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"INTERNAL_ERROR\", \"message\": \"Произошла внутренняя ошибка сервера\", \"status\": 500, \"timestamp\": \"2025-07-21T14:10:00.000\", \"data\": null }"
                                    )
                            )
                    )
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long id) {
        UserResponse data = userService.getUserResponseById(id);
        return ResponseEntity.ok(ApiResponse.successWithData(data, null));
    }

    @Operation(
            summary = "Получить пользователя по username",
            description = "Только для администратора",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Пользователь найден",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = """
{
  "error": null,
  "message": null,
  "status": 200,
  "timestamp": "2025-07-21T13:00:00.000",
  "data": [
    { ... }
  ]
}
"""
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Пользователь не найден",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"USER_NOT_FOUND\", \"message\": \"Пользователь не найден\", \"status\": 404, \"timestamp\": \"2025-07-21T14:10:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Пользователь не авторизован",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"USER_NOT_AUTHENTICATED\", \"message\": \"Пользователь не авторизован в системе\", \"status\": 401, \"timestamp\": \"2025-07-21T14:10:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Доступ запрещён: недостаточно прав",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"FORBIDDEN\", \"message\": \"Действие запрещено: недостаточно прав\", \"status\": 403, \"timestamp\": \"2025-07-21T14:10:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "500",
                            description = "Внутренняя ошибка сервера",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"INTERNAL_ERROR\", \"message\": \"Произошла внутренняя ошибка сервера\", \"status\": 500, \"timestamp\": \"2025-07-21T14:10:00.000\", \"data\": null }"
                                    )
                            )
                    )
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse> getUserByUsername(@PathVariable String username) {
        UserResponse data = userService.getUserResponseByUsername(username);
        return ResponseEntity.ok(ApiResponse.successWithData(data, null));
    }
}