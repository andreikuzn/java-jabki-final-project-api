package bookShop.controller;

import bookShop.model.Book;
import bookShop.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@Tag(name = "Книги", description = "CRUD для книг")
@RestController
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @Operation(
            summary = "Получить список всех книг",
            description = "Возвращает все книги в библиотеке"
    )

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    @Operation(
            summary = "Добавить книгу",
            description = "Добавить новую книгу в библиотеку (только админ)"
    )

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Book addBook(@RequestBody Book book) {
        return bookService.addBook(book);
    }

    @Operation(
            summary = "Изменить книгу",
            description = "Изменить данные книги по ID (только админ)"
    )

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Book updateBook(@PathVariable Long id, @RequestBody Book book) {
        return bookService.updateBook(id, book);
    }

    @Operation(
            summary = "Удалить книгу",
            description = "Удалить книгу по ID (только админ)"
    )

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
    }
}