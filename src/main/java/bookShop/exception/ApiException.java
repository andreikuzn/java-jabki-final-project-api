package bookShop.exception;

import lombok.*;

@lombok.Getter
public class ApiException extends RuntimeException {
    private final String errorCode;
    private final int status;

    public ApiException(String errorCode, String message, int status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }
}