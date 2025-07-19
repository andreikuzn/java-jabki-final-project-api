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

import static bookShop.util.ApiResponseUtil.success;
import static bookShop.util.SwaggerResponses.*;

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
                                            value = STATUS_200_MSG_LOAN_ISSUE
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
                                                    value = STATUS_400_LIMIT_EXCEEDED
                                            ),
                                            @ExampleObject(
                                                    name = "Нет экземпляров",
                                                    value = STATUS_400_BOOK_UNAVAILABLE
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
                                            value = STATUS_401
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Доступ запрещён: недостаточно прав",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = STATUS_403
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
                                                    value = STATUS_404_BOOK
                                            ),
                                            @ExampleObject(
                                                    name = "Пользователь не найден",
                                                    value = STATUS_404_USER
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
                                            value = STATUS_500
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
        LoanResponse data = LoanResponse.from(loanService.issueBook(bookId, userDetails.getId()));
        return success(data, "Книга успешно выдана");
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
                                            value = STATUS_200_MSG_LOAN_RETURN
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Книга уже возвращена",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = STATUS_400_ALREADY_RETURNED
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Пользователь не авторизован",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = STATUS_401
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Доступ запрещён: недостаточно прав",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = STATUS_403
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
                                                    value = STATUS_404_LOAN
                                            ),
                                            @ExampleObject(
                                                    name = "Пользователь не найден",
                                                    value = STATUS_404_USER
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
                                            value = STATUS_500
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
        LoanResponse data = LoanResponse.from(loanService.returnBook(loanId, userDetails.getId()));
        return success(data, "Книга успешно возвращена");
    }

    @Operation(
            summary = "Мои активные займы",
            description = "Список всех не возвращённых займов пользователя",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Список выдач",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = STATUS_200_LIST
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Выдача не найдена",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = STATUS_404_LOAN
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Пользователь не авторизован",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = STATUS_401
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "500",
                            description = "Внутренняя ошибка сервера",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = STATUS_500
                                    )
                            )
                    )
            }
    )
    @GetMapping("/my")
    public ResponseEntity<ApiResponse> myLoans(Authentication authentication) {
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        List<LoanResponse> data = loanService.getActiveLoans(userDetails.getId())
                .stream()
                .map(LoanResponse::from)
                .collect(Collectors.toList());
        return success(data);
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
                                            value = STATUS_200_LIST
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
                                                    value = STATUS_404_LOAN
                                            ),
                                            @ExampleObject(
                                                    name = "Книга не найдена",
                                                    value = STATUS_404_BOOK
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
                                            value = STATUS_401
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Доступ запрещён: недостаточно прав",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = STATUS_403
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "500",
                            description = "Внутренняя ошибка сервера",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = STATUS_500
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
        return success(data);
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
                                            value = STATUS_200_LIST
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
                                                    value = STATUS_404_LOAN
                                            ),
                                            @ExampleObject(
                                                    name = "Пользователь не найден",
                                                    value = STATUS_404_USER
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
                                            value = STATUS_401
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Доступ запрещён: недостаточно прав",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = STATUS_403
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "500",
                            description = "Внутренняя ошибка сервера",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = STATUS_500
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
        return success(data);
    }
}