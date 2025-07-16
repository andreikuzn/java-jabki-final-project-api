package bookShop.controller;

import bookShop.model.Loan;
import bookShop.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Выдача книг", description = "Оформление и возврат книг пользователями")
@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @Operation(
            summary = "Получить все выдачи",
            description = "Только для администратора. Возвращает список всех выдач."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<Loan> getAllLoans() {
        return loanService.getAllLoans();
    }

    @Operation(
            summary = "Получить свои выдачи",
            description = "Текущий пользователь получает список своих выдач."
    )
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my")
    public List<Loan> getMyLoans(Authentication authentication) {
        String username = authentication.getName();
        return loanService.getLoansByUsername(username);
    }

    @Operation(
            summary = "Взять книгу",
            description = "Пользователь берет книгу на руки"
    )
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/issue")
    public Loan issueBook(@RequestParam Long bookId, Authentication authentication) {
        String username = authentication.getName();
        return loanService.issueBook(bookId, username);
    }

    @Operation(
            summary = "Вернуть книгу",
            description = "Пользователь возвращает книгу"
    )
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/return")
    public Loan returnBook(@RequestParam Long loanId, Authentication authentication) {
        String username = authentication.getName();
        return loanService.returnBook(loanId, username);
    }
}