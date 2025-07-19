package bookShop.service;

import bookShop.model.*;
import bookShop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final AppUserRepository appUserRepository;
    private final LoyaltyService loyaltyService;
    private final bookShop.validation.LoanValidator loanValidator;

    @Transactional
    public Loan issueBook(Long bookId, Long userId) {
        log.info("Пользователь [{}] берёт книгу [{}]", userId, bookId);
        AppUser user = loanValidator.validateAndGetUser(userId);
        Book book = loanValidator.validateAndGetBook(bookId);
        LoyaltyLevel level = user.getLoyaltyLevel();
        if (user.getLoyaltyPoints() < 0) level = LoyaltyLevel.NOVICE;
        loanValidator.checkActiveLoansLimit(user, level);
        loanValidator.checkBookPriceLimit(book, level);
        loanValidator.checkBookAvailability(book);
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
        AppUser user = loanValidator.validateAndGetUser(userId);
        Loan loan = loanValidator.validateAndGetLoan(loanId);
        loanValidator.checkLoanOwnership(loan, userId);
        loanValidator.checkLoanNotReturned(loan);
        loan.setReturnedDate(LocalDate.now());
        boolean overdue = loan.getReturnedDate().isAfter(loan.getDueDate());
        user.setLoyaltyPoints(user.getLoyaltyPoints() + (overdue ? -2 : 1));
        user.setLoyaltyLevel(loyaltyService.calculateLevel(user.getLoyaltyPoints()));
        appUserRepository.save(user);
        loanRepository.save(loan);
        Book book = loan.getBook();
        book.setCopiesAvailable(book.getCopiesAvailable() + 1);
        bookRepository.save(book);
        log.info("Книга по займу [{}] возвращена пользователем [{}]", loanId, userId);
        return loan;
    }

    public List<Loan> getActiveLoans(Long userId) {
        loanValidator.validateAndGetUser(userId);
        List<Loan> loans = loanRepository.findByAppUserIdAndReturnedDateIsNull(userId);
        loanValidator.checkLoansNotEmpty(loans);
        return loans;
    }

    public List<Loan> getActiveLoansByBook(Long bookId) {
        loanValidator.validateAndGetBook(bookId);
        List<Loan> loans = loanRepository.findByBookIdAndReturnedDateIsNull(bookId);
        loanValidator.checkLoansNotEmpty(loans);
        return loans;
    }
}