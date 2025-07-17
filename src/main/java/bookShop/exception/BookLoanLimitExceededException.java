package bookShop.exception;

public class BookLoanLimitExceededException extends ApiException {
    public BookLoanLimitExceededException() {
        super("BOOK_LOAN_LIMIT_EXCEEDED", "Превышен лимит книг для вашего уровня лояльности", 400);
    }
    public BookLoanLimitExceededException(String message) {
        super("BOOK_LOAN_LIMIT_EXCEEDED", message, 400);
    }
}