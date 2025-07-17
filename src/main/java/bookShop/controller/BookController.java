package bookShop.controller;

import bookShop.model.BookResponse;
import bookShop.model.Book;
import bookShop.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Книги", description = "CRUD для книг")
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
    public BookResponse addBook(@RequestBody Book book) {
        return BookResponse.from(bookService.addBook(book));
    }

    @Operation(summary = "Изменить книгу", description = "Изменить данные книги по ID (только админ)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public BookResponse updateBook(@PathVariable Long id, @RequestBody Book book) {
        return BookResponse.from(bookService.updateBook(id, book));
    }

    @Operation(summary = "Удалить книгу", description = "Удалить книгу по ID (только админ)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
    }
}