package bookShop.exception;

public class BookAlreadyExistsException extends ApiException {
    public BookAlreadyExistsException(String message) {
        super("BOOK_ALREADY_EXISTS", message, 400);
    }
}