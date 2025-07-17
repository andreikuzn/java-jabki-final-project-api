package bookShop.exception;

public class ForbiddenActionException extends ApiException {
    public ForbiddenActionException() {
        super("FORBIDDEN", "Действие запрещено", 403);
    }
    public ForbiddenActionException(String message) {
        super("FORBIDDEN", message, 403);
    }
}