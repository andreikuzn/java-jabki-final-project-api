package bookShop.exception;

public class LoanAlreadyReturnedException extends ApiException {
    public LoanAlreadyReturnedException() {
        super("LOAN_ALREADY_RETURNED", "Книга уже возвращена", 400);
    }
    public LoanAlreadyReturnedException(String message) {
        super("LOAN_ALREADY_RETURNED", message, 400);
    }
}