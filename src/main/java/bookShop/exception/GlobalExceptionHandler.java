package bookShop.exception;

import bookShop.model.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse> handleApiException(ApiException ex) {
        log.error("API ошибка [{}]: {}", ex.getErrorCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.getStatus())
                .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage(), ex.getStatus()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationError(MethodArgumentNotValidException ex) {
        StringBuilder sb = new StringBuilder();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                sb.append(err.getField()).append(": ").append(err.getDefaultMessage()).append("; "));
        log.warn("Ошибка валидации запроса: {}", sb);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("VALIDATION_ERROR", sb.toString(), 400));
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException ex) {
        log.warn("Доступ запрещён: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("FORBIDDEN", "Действие запрещено: недостаточно прав", 403));
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ApiResponse> handleAuthException(org.springframework.security.core.AuthenticationException ex) {
        log.warn("Пользователь не авторизован: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("USER_NOT_AUTHENTICATED", "Пользователь не авторизован в системе", 401));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleOtherExceptions(Exception ex) {
        log.error("Необработанная ошибка: ", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", ex.getMessage(), 500));
    }
}