package bookShop.controller;

import bookShop.model.BookResponse;
import bookShop.model.BookRequest;
import bookShop.model.ApiResponse;
import bookShop.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Книги", description = "Управление книгами")
@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @Operation(
            summary = "Получить список всех книг",
            description = "Возвращает все книги в библиотеке",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Список книг",
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
                            description = "Книги не найдены",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"BOOK_NOT_FOUND\", \"message\": \"Книги не найдены\", \"status\": 404, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Пользователь не авторизован",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"USER_NOT_AUTHENTICATED\", \"message\": \"Пользователь не авторизован в системе\", \"status\": 401, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
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
    @GetMapping
    public ResponseEntity<ApiResponse> getAllBooks() {
        List<BookResponse> data = bookService.getAllBooks().stream()
                .map(BookResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.successWithData(data, null));
    }

    @Operation(
            summary = "Добавить книгу",
            description = "Добавить новую книгу (только админ)",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Книга успешно добавлена",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = """
{
  "error": null,
  "message": "Книга успешно добавлена",
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
                            description = "Ошибка валидации или книга уже существует",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "Ошибка валидации",
                                                    value = "{ \"error\": \"VALIDATION_ERROR\", \"message\": \"Название книги должно быть от 2 до 32 символов\", \"status\": 400, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
                                            ),
                                            @ExampleObject(
                                                    name = "Книга уже существует",
                                                    value = "{ \"error\": \"BOOK_ALREADY_EXISTS\", \"message\": \"Книга с таким названием и автором уже существует\", \"status\": 400, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
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
                                            value = "{ \"error\": \"USER_NOT_AUTHENTICATED\", \"message\": \"Пользователь не авторизован в системе\", \"status\": 401, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Доступ запрещён: недостаточно прав",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"FORBIDDEN\", \"message\": \"Действие запрещено: недостаточно прав\", \"status\": 403, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
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
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse> addBook(@Valid @RequestBody BookRequest request) {
        BookResponse data = BookResponse.from(bookService.addBook(request));
        return ResponseEntity.ok(ApiResponse.successWithData(data, "Книга успешно добавлена"));
    }

    @Operation(
            summary = "Изменить книгу",
            description = "Изменить price и copiesAvailable по ID (только админ)",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Книга успешно обновлена",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = """
{
  "error": null,
  "message": "Книга успешно обновлена",
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
                            description = "Книга не найдена",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"BOOK_NOT_FOUND\", \"message\": \"Книга не найдена\", \"status\": 404, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Ошибка валидации",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"VALIDATION_ERROR\", \"message\": \"Некорректные данные\", \"status\": 400, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Пользователь не авторизован",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"USER_NOT_AUTHENTICATED\", \"message\": \"Пользователь не авторизован в системе\", \"status\": 401, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Доступ запрещён: недостаточно прав",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"FORBIDDEN\", \"message\": \"Действие запрещено: недостаточно прав\", \"status\": 403, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
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
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        BookResponse data = BookResponse.from(bookService.updateBook(id, request));
        return ResponseEntity.ok(ApiResponse.successWithData(data, "Книга успешно обновлена"));
    }

    @Operation(
            summary = "Удалить книгу",
            description = "Удалить книгу по ID (только админ)",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Книга удалена",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": null, \"message\": \"Книга удалена\", \"status\": 200, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Книга не найдена",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"BOOK_NOT_FOUND\", \"message\": \"Книга не найдена\", \"status\": 404, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Пользователь не авторизован",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"USER_NOT_AUTHENTICATED\", \"message\": \"Пользователь не авторизован в системе\", \"status\": 401, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Доступ запрещён: недостаточно прав",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"FORBIDDEN\", \"message\": \"Действие запрещено: недостаточно прав\", \"status\": 403, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
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
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok(ApiResponse.successWithData(null, "Книга удалена"));
    }

    @Operation(
            summary = "Получить книгу по id",
            description = "Только для администратора",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Книга найдена",
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
                            description = "Книга не найдена",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"BOOK_NOT_FOUND\", \"message\": \"Книга не найдена\", \"status\": 404, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Книга не найдена",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"BOOK_NOT_FOUND\", \"message\": \"Книга не найдена\", \"status\": 404, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Пользователь не авторизован",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"USER_NOT_AUTHENTICATED\", \"message\": \"Пользователь не авторизован в системе\", \"status\": 401, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Доступ запрещён: недостаточно прав",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"FORBIDDEN\", \"message\": \"Действие запрещено: недостаточно прав\", \"status\": 403, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
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
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getBookById(@PathVariable Long id) {
        BookResponse data = BookResponse.from(bookService.getBookById(id));
        return ResponseEntity.ok(ApiResponse.successWithData(data, null));
    }

    @Operation(
            summary = "Получить книги по названию",
            description = "Только для администратора",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Книги найдены",
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
                            description = "Книга не найдена",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"BOOK_NOT_FOUND\", \"message\": \"Книга не найдена\", \"status\": 404, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Книги не найдены",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"BOOK_NOT_FOUND\", \"message\": \"Книги с указанным названием не найдены\", \"status\": 404, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Пользователь не авторизован",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"USER_NOT_AUTHENTICATED\", \"message\": \"Пользователь не авторизован в системе\", \"status\": 401, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Доступ запрещён: недостаточно прав",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"FORBIDDEN\", \"message\": \"Действие запрещено: недостаточно прав\", \"status\": 403, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
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
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{title}")
    public ResponseEntity<ApiResponse> getBooksByTitle(@PathVariable String title) {
        List<BookResponse> data = bookService.getBooksByTitle(title).stream()
                .map(BookResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.successWithData(data, null));
    }

    @Operation(
            summary = "Получить книги по автору",
            description = "Только для администратора",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Книги найдены",
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
                            description = "Книги не найдены",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"BOOK_NOT_FOUND\", \"message\": \"Книги с указанным автором не найдены\", \"status\": 404, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Пользователь не авторизован",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"USER_NOT_AUTHENTICATED\", \"message\": \"Пользователь не авторизован в системе\", \"status\": 401, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Доступ запрещён: недостаточно прав",
                            content = @Content(
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            value = "{ \"error\": \"FORBIDDEN\", \"message\": \"Действие запрещено: недостаточно прав\", \"status\": 403, \"timestamp\": \"2025-07-21T13:00:00.000\", \"data\": null }"
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
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{author}")
    public ResponseEntity<ApiResponse> getBooksByAuthor(@PathVariable String author) {
        List<BookResponse> data = bookService.getBooksByAuthor(author).stream()
                .map(BookResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.successWithData(data, null));
    }
}