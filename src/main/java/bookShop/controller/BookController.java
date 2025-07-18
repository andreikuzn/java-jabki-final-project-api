package bookShop.controller;

import bookShop.model.BookResponse;
import bookShop.model.BookRequest;
import bookShop.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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

    @Operation(summary = "Получить список всех книг", description = "Возвращает все книги в библиотеке")
    @GetMapping
    public List<BookResponse> getAllBooks() {
        return bookService.getAllBooks().stream()
                .map(BookResponse::from)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Добавить книгу", description = "Добавить новую книгу (только админ)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public BookResponse addBook(@Valid @RequestBody BookRequest request) {
        return BookResponse.from(bookService.addBook(request));
    }

    @Operation(summary = "Изменить книгу", description = "Изменить price и copiesAvailable по ID (только админ)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public BookResponse updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return BookResponse.from(bookService.updateBook(id, request));
    }

    @Operation(summary = "Удалить книгу", description = "Удалить книгу по ID (только админ)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
    }

    @Operation(summary = "Получить книгу по id", description = "Только для администратора")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public BookResponse getBookById(@PathVariable Long id) {
        return BookResponse.from(bookService.getBookById(id));
    }

    @Operation(summary = "Получить книги по названию", description = "Только для администратора")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/by-title/{title}")
    public List<BookResponse> getBooksByTitle(@PathVariable String title) {
        return bookService.getBooksByTitle(title).stream()
                .map(BookResponse::from)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Получить книги по автору", description = "Только для администратора")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/by-author/{author}")
    public List<BookResponse> getBooksByAuthor(@PathVariable String author) {
        return bookService.getBooksByAuthor(author).stream()
                .map(BookResponse::from)
                .collect(Collectors.toList());
    }
}