package bookShop.exception;

public class AdminLimitExceededException extends ApiException {
    public AdminLimitExceededException() {
        super("ADMIN_LIMIT_EXCEEDED", "Нельзя регистрировать больше 3-х администраторов", 403);
    }
    public AdminLimitExceededException(String message) {
        super("ADMIN_LIMIT_EXCEEDED", message, 403);
    }
}