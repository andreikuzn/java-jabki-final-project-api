package bookShop.exception;

public class InvalidCredentialsException extends ApiException {
    public InvalidCredentialsException() {
        super("INVALID_CREDENTIALS", "Неверный логин или пароль", 401);
    }
    public InvalidCredentialsException(String message) {
        super("INVALID_CREDENTIALS", message, 401);
    }
}
