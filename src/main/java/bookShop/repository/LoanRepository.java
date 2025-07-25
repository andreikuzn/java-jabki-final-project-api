package bookShop.repository;

import bookShop.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByAppUserIdAndReturnedDateIsNull(Long userId);
    List<Loan> findByBookIdAndReturnedDateIsNull(Long bookId);
}
