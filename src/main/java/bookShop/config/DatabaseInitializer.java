package bookShop.config;

import bookShop.model.Book;
import bookShop.model.Role;
import bookShop.repository.BookRepository;
import bookShop.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Пользователь-админ
        if (!appUserRepository.existsByUsername("admin")) {
            bookShop.model.AppUser admin = bookShop.model.AppUser.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.USER)
                    .build();
            appUserRepository.save(admin);
            System.out.println("Создан пользователь-администратор: admin/admin123");
        }

        // Пользователь-читатель
        if (!appUserRepository.existsByUsername("reader")) {
            bookShop.model.AppUser appUser = bookShop.model.AppUser.builder()
                    .username("reader")
                    .password(passwordEncoder.encode("reader123"))
                    .role(Role.USER)
                    .build();
            appUserRepository.save(appUser);
            System.out.println("Создан пользователь: reader/reader123");
        }

        // Несколько книг
        if (bookRepository.count() == 0) {
            Book book1 = Book.builder()
                    .title("Clean Code")
                    .author("Robert C. Martin")
                    .copiesAvailable(3)
                    .build();

            Book book2 = Book.builder()
                    .title("Spring in Action")
                    .author("Craig Walls")
                    .copiesAvailable(2)
                    .build();

            Book book3 = Book.builder()
                    .title("Java: The Complete Reference")
                    .author("Herbert Schildt")
                    .copiesAvailable(4)
                    .build();

            bookRepository.saveAll(Arrays.asList(book1, book2, book3));
            System.out.println("В базу добавлены тестовые книги.");
        }
    }
}