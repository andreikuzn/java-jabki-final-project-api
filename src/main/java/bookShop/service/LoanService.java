package bookShop.service;

import bookShop.model.AppUser;
import bookShop.model.Book;
import bookShop.model.Loan;
import bookShop.repository.AppUserRepository;
import bookShop.repository.BookRepository;
import bookShop.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final AppUserRepository appUserRepository;

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    public List<Loan> getLoansByUsername(String username) {
        return loanRepository.findByAppUserUsername(username);
    }

    public Loan issueBook(Long bookId, String username) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Книга не найдена"));

        if (book.getCopiesAvailable() <= 0) {
            throw new RuntimeException("Отсутствуют доступные к выдаче экземпляры");
        }

        book.setCopiesAvailable(book.getCopiesAvailable() - 1);
        bookRepository.save(book);

        Loan loan = Loan.builder()
                .appUser(user)
                .book(book)
                .loanDate(LocalDate.now())
                .returnDate(null)
                .build();

        return loanRepository.save(loan);
    }

    public Loan returnBook(Long loanId, String username) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Не найдена оформленная выдача"));

        if (!loan.getAppUser().getUsername().equals(username)) {
            throw new RuntimeException("Пользователь не может вернуть книгу, которую ему не выдавали");
        }
        if (loan.getReturnDate() != null) {
            throw new RuntimeException("Книга уже возвращена");
        }

        loan.setReturnDate(LocalDate.now());

        Book book = loan.getBook();
        book.setCopiesAvailable(book.getCopiesAvailable() + 1);
        bookRepository.save(book);

        return loanRepository.save(loan);
    }
}