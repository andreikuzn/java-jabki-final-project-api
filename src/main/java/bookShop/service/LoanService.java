package bookShop.service;

import bookShop.model.Book;
import bookShop.model.Loan;
import bookShop.repository.BookRepository;
import bookShop.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class LoanService {

    @Autowired
    private LoanRepository loanRepo;
    @Autowired
    private BookRepository bookRepo;
    @Autowired
    private bookShop.repository.AppUserRepository userRepo;

    public List<Loan> getAllLoans() {
        return loanRepo.findAll();
    }

    public Loan issueBook(Long bookId, String username) {
        Book book = bookRepo.findById(bookId).orElseThrow(() -> new RuntimeException("Book not found"));
        if (book.getCopiesAvailable() <= 0)
            throw new RuntimeException("No available copies");

        bookShop.model.AppUser appUser = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        book.setCopiesAvailable(book.getCopiesAvailable() - 1);
        bookRepo.save(book);

        Loan loan = Loan.builder()
                .appUser(appUser)
                .book(book)
                .loanDate(LocalDate.now())
                .build();
        return loanRepo.save(loan);
    }

    public Loan returnBook(Long loanId) {
        Loan loan = loanRepo.findById(loanId).orElseThrow(() -> new RuntimeException("Loan not found"));
        if (loan.getReturnDate() != null)
            throw new RuntimeException("Book already returned");

        loan.setReturnDate(LocalDate.now());

        Book book = loan.getBook();
        book.setCopiesAvailable(book.getCopiesAvailable() + 1);
        bookRepo.save(book);

        return loanRepo.save(loan);
    }
}