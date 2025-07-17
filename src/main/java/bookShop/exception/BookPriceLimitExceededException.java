package bookShop.exception;

public class BookPriceLimitExceededException extends ApiException {
    public BookPriceLimitExceededException() {
        super("BOOK_PRICE_LIMIT_EXCEEDED", "Стоимость книги превышает разрешенную для вашего уровня", 400);
    }
    public BookPriceLimitExceededException(String message) {
        super("BOOK_PRICE_LIMIT_EXCEEDED", message, 400);
    }
}
