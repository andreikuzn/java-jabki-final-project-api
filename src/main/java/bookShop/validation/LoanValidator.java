package bookShop.validation;

import bookShop.model.*;
import bookShop.repository.*;
import bookShop.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LoanValidator {

    private final AppUserRepository userRepository;
    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;

    public AppUser validateAndGetUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
    }

    public Book validateAndGetBook(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Книга не найдена"));
    }

    public Loan validateAndGetLoan(Long loanId) {
        return loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException("Выдача книги не найдена"));
    }

    public void checkLoanOwnership(Loan loan, Long userId) {
        if (!loan.getAppUser().getId().equals(userId)) {
            throw new ForbiddenActionException("Вы не можете вернуть чужую книгу");
        }
    }

    public void checkLoanNotReturned(Loan loan) {
        if (loan.getReturnedDate() != null) {
            throw new LoanAlreadyReturnedException("Книга уже возвращена");
        }
    }

    public void checkActiveLoansLimit(AppUser user, LoyaltyLevel level) {
        int currentActiveLoans = loanRepository.findByAppUserIdAndReturnedDateIsNull(user.getId()).size();
        if (currentActiveLoans >= level.getMaxBooks()) {
            throw new BookLoanLimitExceededException("Превышен лимит книг для вашего уровня лояльности (" + level.getTitle() + ")");
        }
    }

    public void checkBookPriceLimit(Book book, LoyaltyLevel level) {
        if (book.getPrice() > level.getMaxBookPrice()) {
            throw new BookPriceLimitExceededException("Стоимость книги превышает разрешенную для вашего уровня (" + level.getTitle() + ")");
        }
    }

    public void checkBookAvailability(Book book) {
        if (book.getCopiesAvailable() <= 0) {
            throw new BookUnavailableException("Нет доступных экземпляров книги");
        }
    }

    public void checkLoansNotEmpty(List<Loan> loans) {
        if (loans.isEmpty()) {
            throw new LoanNotFoundException("Выдачи не найдены");
        }
    }
}