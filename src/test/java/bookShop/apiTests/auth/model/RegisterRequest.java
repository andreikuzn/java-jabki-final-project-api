package bookShop.apiTests.auth.model;

import lombok.Data;
import lombok.Builder;

@Data
@Builder(toBuilder = true)
public class RegisterRequest {
    private String username;
    private String password;
    private String role;
    private String phone;
    private String email;
}