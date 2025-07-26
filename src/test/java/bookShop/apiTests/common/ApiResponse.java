package bookShop.apiTests.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String error;
    private String message;
    private int status;
    private String timestamp;
    private T data;
}