package bookShop.controller;

import bookShop.model.Loan;
import bookShop.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loans")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @GetMapping
    public List<Loan> getAllLoans() {
        return loanService.getAllLoans();
    }

    @PostMapping("/issue")
    public Loan issueBook(@RequestParam Long bookId, Authentication authentication) {
        String username = authentication.getName();
        return loanService.issueBook(bookId, username);
    }

    @PostMapping("/return")
    public Loan returnBook(@RequestParam Long loanId) {
        return loanService.returnBook(loanId);
    }
}