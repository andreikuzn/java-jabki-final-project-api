package bookShop.exception;

public class UserNotFoundException extends ApiException {
    public UserNotFoundException() {
        super("USER_NOT_FOUND", "Пользователь не найден", 404);
    }
    public UserNotFoundException(String message) {
        super("USER_NOT_FOUND", message, 404);
    }
}