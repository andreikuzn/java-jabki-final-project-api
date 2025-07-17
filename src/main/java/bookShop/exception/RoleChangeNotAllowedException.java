package bookShop.exception;

public class RoleChangeNotAllowedException extends ApiException {
    public RoleChangeNotAllowedException() {
        super("ROLE_CHANGE_NOT_ALLOWED", "Изменение роли запрещено", 403);
    }
    public RoleChangeNotAllowedException(String message) {
        super("ROLE_CHANGE_NOT_ALLOWED", message, 403);
    }
}