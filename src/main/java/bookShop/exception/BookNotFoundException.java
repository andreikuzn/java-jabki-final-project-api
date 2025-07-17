package bookShop.exception;

public class BookNotFoundException extends ApiException {
    public BookNotFoundException() {
        super("BOOK_NOT_FOUND", "Книга не найдена", 404);
    }
    public BookNotFoundException(String message) {
        super("BOOK_NOT_FOUND", message, 404);
    }
}