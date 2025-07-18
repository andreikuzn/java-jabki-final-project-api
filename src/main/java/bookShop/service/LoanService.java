package bookShop.service;

import bookShop.model.*;
import bookShop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import bookShop.exception.UserNotFoundException;
import bookShop.exception.BookNotFoundException;
import bookShop.exception.BookUnavailableException;
import bookShop.exception.BookLoanLimitExceededException;
import bookShop.exception.LoanNotFoundException;
import bookShop.exception.ForbiddenActionException;
import  bookShop.exception.LoanAlreadyReturnedException;
import bookShop.exception.BookPriceLimitExceededException;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final AppUserRepository appUserRepository;
    private final LoyaltyService loyaltyService;

    @Transactional
    public Loan issueBook(Long bookId, Long userId) {
        log.info("Пользователь [{}] берёт книгу [{}]", userId, bookId);
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Книга не найдена"));
        LoyaltyLevel level = user.getLoyaltyLevel();
        int currentActiveLoans = loanRepository.findByAppUserIdAndReturnedDateIsNull(userId).size();
        if (user.getLoyaltyPoints() < 0) {
            level = LoyaltyLevel.NOVICE;
        }
        if (currentActiveLoans >= level.getMaxBooks()) {
            throw new BookLoanLimitExceededException("Превышен лимит книг для вашего уровня лояльности (" + level.getTitle() + ")");
        }
        if (book.getPrice() > level.getMaxBookPrice()) {
            throw new BookPriceLimitExceededException("Стоимость книги превышает разрешенную для вашего уровня (" + level.getTitle() + ")");
        }
        if (book.getCopiesAvailable() <= 0) {
            throw new BookUnavailableException("Нет доступных экземпляров книги");
        }
        book.setCopiesAvailable(book.getCopiesAvailable() - 1);
        bookRepository.save(book);
        Loan loan = Loan.builder()
                .appUser(user)
                .book(book)
                .loanDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(level.getMaxDays()))
                .build();
        log.info("Книга [{}] выдана пользователю [{}]", bookId, userId);
        return loanRepository.save(loan);
    }

    @Transactional
    public Loan returnBook(Long loanId, Long userId) {
        log.info("Пользователь [{}] возвращает книгу по выдаче [{}]", userId, loanId);
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Выдача книги не найдена"));
        if (!loan.getAppUser().getId().equals(userId)) {
            throw new ForbiddenActionException("Вы не можете вернуть чужую книгу");
        }
        if (loan.getReturnedDate() != null) {
            throw new LoanAlreadyReturnedException("Книга уже возвращена");
        }
        loan.setReturnedDate(LocalDate.now());
        AppUser user = loan.getAppUser();
        boolean overdue = loan.getReturnedDate().isAfter(loan.getDueDate());
        if (overdue) {
            user.setLoyaltyPoints(user.getLoyaltyPoints() - 2);
        } else {
            user.setLoyaltyPoints(user.getLoyaltyPoints() + 1);
        }
        LoyaltyLevel newLevel = loyaltyService.calculateLevel(user.getLoyaltyPoints());
        user.setLoyaltyLevel(newLevel);
        appUserRepository.save(user);
        loanRepository.save(loan);
        Book book = loan.getBook();
        book.setCopiesAvailable(book.getCopiesAvailable() + 1);
        bookRepository.save(book);
        log.info("Книга по займу [{}] возвращена пользователем [{}]", loanId, userId);
        return loan;
    }

    public List<Loan> getActiveLoans(Long userId) {
        return loanRepository.findByAppUserIdAndReturnedDateIsNull(userId);
    }

    public List<Loan> getActiveLoansByBook(Long bookId) {
        return loanRepository.findByBookIdAndReturnedDateIsNull(bookId);
    }
}