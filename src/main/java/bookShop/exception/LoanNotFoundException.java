package bookShop.exception;

public class LoanNotFoundException extends ApiException {
    public LoanNotFoundException() {
        super("LOAN_NOT_FOUND", "Выдача не найдена", 404);
    }
    public LoanNotFoundException(String message) {
        super("LOAN_NOT_FOUND", message, 404);
    }
}