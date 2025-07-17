package bookShop.exception;

public class BookLimitExceededException extends ApiException {
    public BookLimitExceededException() {
        super("BOOK_LIMIT_EXCEEDED", "Превышен лимит книг для вашего уровня лояльности", 400);
    }
    public BookLimitExceededException(String message) {
        super("BOOK_LIMIT_EXCEEDED", message, 400);
    }
}
