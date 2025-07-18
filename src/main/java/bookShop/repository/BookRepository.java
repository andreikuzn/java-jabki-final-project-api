package bookShop.repository;

import bookShop.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<bookShop.model.Book> findByTitle(String title);
    List<Book> findByAuthor(String author);
    boolean existsByTitleAndAuthor(String title, String author);
}


