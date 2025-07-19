package bookShop.util;

import org.springframework.http.ResponseEntity;
import bookShop.model.request.AuthRequest;

public class ApiResponseUtil {

    private ApiResponseUtil() {

    }

    public static ResponseEntity<ApiResponse> success(Object data) {
        return ResponseEntity.ok(ApiResponse.successWithData(data, null));
    }

    public static ResponseEntity<ApiResponse> success(Object data, String message) {
        return ResponseEntity.ok(ApiResponse.successWithData(data, message));
    }

    public static ResponseEntity<ApiResponse> successMsg(String message) {
        return ResponseEntity.ok(ApiResponse.successWithData(null, message));
    }

    public static ResponseEntity<ApiResponse> error(ApiResponse error) {
        return ResponseEntity.status(error.getStatus()).body(error);
    }
}