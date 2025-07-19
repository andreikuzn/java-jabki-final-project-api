package bookShop.controller;

import bookShop.model.LoanResponse;
import bookShop.model.ApiResponse;
import bookShop.model.AppUserDetails;
import bookShop.service.LoanService;
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

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Выдача книг", description = "Выдача и возврат книг")
@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @Operation(
            summary = "Взять книгу",
            description = "Оформить новую выдачу книги (учитывает лимиты по уровню)",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Книга успешно выдана",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = """
{
  "error": null,
  "message": "Книга успешно выдана",
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
                            description = "Ошибка: лимиты, валидация, недоступна",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "Лимит книг",
                                                    value = "{ \"error\": \"BOOK_LOAN_LIMIT_EXCEEDED\", \"message\": \"Превышен лимит книг для вашего уровня лояльности\", \"status\": 400, \"timestamp\": \"2025-07-21T14:00:00.000\", \"data\": null }"
                                            ),
                                            @ExampleObject(
                                                    name = "Нет экземпляров",
                                                    value = "{ \"error\": \"BOOK_UNAVAILABLE\", \"message\": \"Нет доступных экземпляров книги\", \"status\": 400, \"timestamp\": \"2025-07-21T14:00:00.000\", \"data\": null }"
                                            )
                                    }
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Пользователь не авторизован",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"USER_NOT_AUTHENTICATED\", \"message\": \"Пользователь не авторизован в системе\", \"status\": 401, \"timestamp\": \"2025-07-21T14:00:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Доступ запрещён: недостаточно прав",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"FORBIDDEN\", \"message\": \"Действие запрещено: недостаточно прав\", \"status\": 403, \"timestamp\": \"2025-07-21T14:00:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Книга или пользователь не найдены",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "Книга не найдена",
                                                    value = "{ \"error\": \"BOOK_NOT_FOUND\", \"message\": \"Книга не найдена\", \"status\": 404, \"timestamp\": \"2025-07-21T14:00:00.000\", \"data\": null }"
                                            ),
                                            @ExampleObject(
                                                    name = "Пользователь не найден",
                                                    value = "{ \"error\": \"USER_NOT_FOUND\", \"message\": \"Пользователь не найдена\", \"status\": 404, \"timestamp\": \"2025-07-21T14:00:00.000\", \"data\": null }"
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
                                            value = "{ \"error\": \"INTERNAL_ERROR\", \"message\": \"Произошла внутренняя ошибка сервера\", \"status\": 500, \"timestamp\": \"2025-07-21T14:00:00.000\", \"data\": null }"
                                    )
                            )
                    )
            }
    )
    @PostMapping("/issue")
    public ResponseEntity<ApiResponse> issueBook(
            @Valid @RequestParam @NotNull(message = "ID книги обязателен") @Min(value = 1, message = "ID книги должен быть положительным") Long bookId,
            Authentication authentication
    ) {
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();
        LoanResponse data = LoanResponse.from(loanService.issueBook(bookId, userId));
        return ResponseEntity.ok(ApiResponse.successWithData(data, "Книга успешно выдана"));
    }

    @Operation(
            summary = "Вернуть книгу",
            description = "Возвратить книгу (учитывается просрочка и начисляются/списываются баллы)",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Книга успешно возвращена",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = """
{
  "error": null,
  "message": "Книга успешно возвращена",
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
                            description = "Книга уже возвращена",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"LOAN_ALREADY_RETURNED\", \"message\": \"Книга уже возвращена\", \"status\": 400, \"timestamp\": \"2025-07-21T14:00:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Пользователь не авторизован",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"USER_NOT_AUTHENTICATED\", \"message\": \"Пользователь не авторизован в системе\", \"status\": 401, \"timestamp\": \"2025-07-21T14:00:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Доступ запрещён: недостаточно прав",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"FORBIDDEN\", \"message\": \"Действие запрещено: недостаточно прав\", \"status\": 403, \"timestamp\": \"2025-07-21T14:00:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Выдача или пользователь не найдены",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "Выдача не найдена",
                                                    value = "{ \"error\": \"LOAN_NOT_FOUND\", \"message\": \"Выдача книги не найдена\", \"status\": 404, \"timestamp\": \"2025-07-21T14:00:00.000\", \"data\": null }"
                                            ),
                                            @ExampleObject(
                                                    name = "Пользователь не найден",
                                                    value = "{ \"error\": \"USER_NOT_FOUND\", \"message\": \"Пользователь не найдена\", \"status\": 404, \"timestamp\": \"2025-07-21T14:00:00.000\", \"data\": null }"
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
                                            value = "{ \"error\": \"INTERNAL_ERROR\", \"message\": \"Произошла внутренняя ошибка сервера\", \"status\": 500, \"timestamp\": \"2025-07-21T14:00:00.000\", \"data\": null }"
                                    )
                            )
                    )
            }
    )
    @PostMapping("/return")
    public ResponseEntity<ApiResponse> returnBook(
            @Valid @RequestParam @NotNull(message = "ID займа обязателен") @Min(value = 1, message = "ID займа должен быть положительным") Long loanId,
            Authentication authentication
    ) {
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();
        LoanResponse data = LoanResponse.from(loanService.returnBook(loanId, userId));
        return ResponseEntity.ok(ApiResponse.successWithData(data, "Книга успешно возвращена"));
    }

    @Operation(
            summary = "Мои активные займы",
            description = "Список всех не возвращённых займов пользователя",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Список займов",
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
                            description = "Выдача не найдена",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"LOAN_NOT_FOUND\", \"message\": \"Выдача книги не найдена\", \"status\": 404, \"timestamp\": \"2025-07-21T14:00:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Пользователь не авторизован",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"USER_NOT_AUTHENTICATED\", \"message\": \"Пользователь не авторизован в системе\", \"status\": 401, \"timestamp\": \"2025-07-21T14:00:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "500",
                            description = "Внутренняя ошибка сервера",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"INTERNAL_ERROR\", \"message\": \"Произошла внутренняя ошибка сервера\", \"status\": 500, \"timestamp\": \"2025-07-21T14:00:00.000\", \"data\": null }"
                                    )
                            )
                    )
            }
    )
    @GetMapping("/my")
    public ResponseEntity<ApiResponse> myLoans(Authentication authentication) {
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();
        List<LoanResponse> data = loanService.getActiveLoans(userId)
                .stream()
                .map(LoanResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.successWithData(data, null));
    }

    @Operation(
            summary = "Получить активные выдачи по id книги",
            description = "Только для админа",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Список активных выдач",
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
                            description = "Выдача или книга не найдены",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "Выдача не найдена",
                                                    value = "{ \"error\": \"LOAN_NOT_FOUND\", \"message\": \"Выдача книги не найдена\", \"status\": 404, \"timestamp\": \"2025-07-21T14:00:00.000\", \"data\": null }"
                                            ),
                                            @ExampleObject(
                                                    name = "Книга не найдена",
                                                    value = "{ \"error\": \"BOOK_NOT_FOUND\", \"message\": \"Книги не найдены\", \"status\": 404, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
                                            )
                                    }
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Пользователь не авторизован",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"USER_NOT_AUTHENTICATED\", \"message\": \"Пользователь не авторизован в системе\", \"status\": 401, \"timestamp\": \"2025-07-21T14:00:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Доступ запрещён: недостаточно прав",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"FORBIDDEN\", \"message\": \"Действие запрещено: недостаточно прав\", \"status\": 403, \"timestamp\": \"2025-07-21T14:00:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "500",
                            description = "Внутренняя ошибка сервера",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"INTERNAL_ERROR\", \"message\": \"Произошла внутренняя ошибка сервера\", \"status\": 500, \"timestamp\": \"2025-07-21T14:00:00.000\", \"data\": null }"
                                    )
                            )
                    )
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{bookId}")
    public ResponseEntity<ApiResponse> getActiveLoansByBook(@PathVariable Long bookId) {
        List<LoanResponse> data = loanService.getActiveLoansByBook(bookId)
                .stream()
                .map(LoanResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.successWithData(data, null));
    }

    @Operation(
            summary = "Получить активные выдачи по id пользователя",
            description = "Только для админа",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Список активных выдач",
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
                            description = "Выдача или пользователь не найдены",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "Выдача не найдена",
                                                    value = "{ \"error\": \"LOAN_NOT_FOUND\", \"message\": \"Выдача книги не найдена\", \"status\": 404, \"timestamp\": \"2025-07-21T14:00:00.000\", \"data\": null }"
                                            ),
                                            @ExampleObject(
                                                    name = "Пользователь не найден",
                                                    value = "{ \"error\": \"USER_NOT_FOUND\", \"message\": \"Пользователь не найдена\", \"status\": 404, \"timestamp\": \"2025-07-21T14:00:00.000\", \"data\": null }"
                                            )
                                    }
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Пользователь не авторизован",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"USER_NOT_AUTHENTICATED\", \"message\": \"Пользователь не авторизован в системе\", \"status\": 401, \"timestamp\": \"2025-07-21T14:00:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Доступ запрещён: недостаточно прав",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"FORBIDDEN\", \"message\": \"Действие запрещено: недостаточно прав\", \"status\": 403, \"timestamp\": \"2025-07-21T14:00:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "500",
                            description = "Внутренняя ошибка сервера",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"INTERNAL_ERROR\", \"message\": \"Произошла внутренняя ошибка сервера\", \"status\": 500, \"timestamp\": \"2025-07-21T14:00:00.000\", \"data\": null }"
                                    )
                            )
                    )
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse> getActiveLoansByUser(@PathVariable Long userId) {
        List<LoanResponse> data = loanService.getActiveLoans(userId)
                .stream()
                .map(LoanResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.successWithData(data, null));
    }
}