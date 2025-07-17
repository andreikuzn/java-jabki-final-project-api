package bookShop.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LoanResponse {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnedDate;

    public static LoanResponse from(Loan loan) {
        LoanResponse dto = new LoanResponse();
        dto.setId(loan.getId());
        dto.setBookId(loan.getBook().getId());
        dto.setBookTitle(loan.getBook().getTitle());
        dto.setLoanDate(loan.getLoanDate());
        dto.setDueDate(loan.getDueDate());
        dto.setReturnedDate(loan.getReturnedDate());
        return dto;
    }
}