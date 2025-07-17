package bookShop.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse {
    private String error;
    private String message;
    private int status;
    private String timestamp;

    public static ApiResponse success(String message) {
        return new ApiResponse(null, message, 200, LocalDateTime.now().toString());
    }

    public static ApiResponse error(String error, String message, int status) {
        return new ApiResponse(error, message, status, LocalDateTime.now().toString());
    }
}