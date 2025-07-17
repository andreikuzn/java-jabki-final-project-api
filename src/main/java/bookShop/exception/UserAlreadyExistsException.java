package bookShop.exception;

public class UserAlreadyExistsException extends ApiException {
    public UserAlreadyExistsException() {
        super("USER_ALREADY_EXISTS", "Пользователь с таким именем уже существует", 400);
    }
    public UserAlreadyExistsException(String message) {
        super("USER_ALREADY_EXISTS", message, 400);
    }
}