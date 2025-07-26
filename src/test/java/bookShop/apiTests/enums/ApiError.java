package bookShop.apiTests.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApiError {
    OK(200, null),
    BAD_REQUEST(400, "VALIDATION_ERROR"),
    FORBIDDEN(403, "ADMIN_LIMIT_EXCEEDED"),
    NOT_FOUND(404, "NOT_FOUND"),
    METHOD_NOT_ALLOWED(405, "METHOD_NOT_ALLOWED"),
    UNSUPPORTED_MEDIA_TYPE(415, "UNSUPPORTED_MEDIA_TYPE"),
    USER_ALREADY_EXISTS(400, "USER_ALREADY_EXISTS");

    private final int status;
    private final String code;
}

