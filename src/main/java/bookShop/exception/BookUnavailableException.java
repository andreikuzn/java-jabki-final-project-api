package bookShop.exception;

public class BookUnavailableException extends ApiException {
    public BookUnavailableException() {
        super("BOOK_UNAVAILABLE", "Нет доступных экземпляров книги", 400);
    }
    public BookUnavailableException(String message) {
        super("BOOK_UNAVAILABLE", message, 400);
    }
}