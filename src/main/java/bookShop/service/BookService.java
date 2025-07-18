package bookShop.service;

import bookShop.model.Book;
import bookShop.model.BookRequest;
import bookShop.repository.BookRepository;
import bookShop.exception.BookNotFoundException;
import bookShop.exception.ForbiddenActionException;
import bookShop.exception.BookAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(BookNotFoundException::new);
    }

    public List<Book> getBooksByTitle(String title) {
        List<Book> books = bookRepository.findByTitle(title);
        if (books.isEmpty()) throw new BookNotFoundException("Книги с указанным названием не найдены");
        return books;
    }

    public List<Book> getBooksByAuthor(String author) {
        List<Book> books = bookRepository.findByAuthor(author);
        if (books.isEmpty()) throw new BookNotFoundException("Книги с указанным автором не найдены");
        return books;
    }

    public Book addBook(BookRequest request) {
        log.info("Добавление книги: [{}] [{}]", request.getTitle(), request.getAuthor());
        request.trimFields();
        if (bookRepository.existsByTitleAndAuthor(request.getTitle(), request.getAuthor())) {
            throw new BookAlreadyExistsException("Книга с таким названием и автором уже существует");
        }
        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .price(request.getPrice())
                .copiesAvailable(request.getCopiesAvailable())
                .build();
        log.info("Книга успешно добавлена: [{}] [{}]", request.getTitle(), request.getAuthor());
        return bookRepository.save(book);
    }


    public Book updateBook(Long id, BookRequest request) {
        log.info("Обновление книги [{}]", id);
        request.trimFields();
        Book book = bookRepository.findById(id)
                .orElseThrow(BookNotFoundException::new);
        if (!book.getTitle().equals(request.getTitle())) {
            throw new ForbiddenActionException("Изменение названия книги запрещено");
        }
        if (!book.getAuthor().equals(request.getAuthor())) {
            throw new ForbiddenActionException("Изменение автора книги запрещено");
        }
        book.setPrice(request.getPrice());
        book.setCopiesAvailable(request.getCopiesAvailable());
        log.info("Книга [{}] успешно обновлена", id);
        return bookRepository.save(book);
    }

    public void deleteBook(Long id) {
        log.warn("Попытка удаления админом книги [{}]", id);
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException();
        }
        bookRepository.deleteById(id);
        log.info("Книга [{}] удалена админом", id);
    }
}