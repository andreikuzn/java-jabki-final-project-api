package bookShop.controller;

import bookShop.model.LoanResponse;
import bookShop.model.AppUserDetails;
import bookShop.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Выдача книг", description = "Оформление и возврат книг")
@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @Operation(summary = "Взять книгу", description = "Оформить новую выдачу книги (учитывает лимиты по уровню)")
    @PostMapping("/issue")
    public LoanResponse issueBook(@RequestParam Long bookId, Authentication authentication) {
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUser().getId();
        return LoanResponse.from(loanService.issueBook(bookId, userId));
    }

    @Operation(summary = "Вернуть книгу", description = "Возвратить книгу (учитывается просрочка и начисляются/списываются баллы)")
    @PostMapping("/return")
    public LoanResponse returnBook(@RequestParam Long loanId, Authentication authentication) {
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUser().getId();
        return LoanResponse.from(loanService.returnBook(loanId, userId));
    }

    @Operation(summary = "Мои активные займы", description = "Список всех не возвращённых займов пользователя")
    @GetMapping("/my")
    public List<LoanResponse> myLoans(Authentication authentication) {
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();
        return loanService.getActiveLoans(userId)
                .stream()
                .map(LoanResponse::from)
                .collect(Collectors.toList());
    }
}