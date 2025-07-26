package bookShop.apiTests.model;

import lombok.Data;
import lombok.Builder;

@Data
@Builder(toBuilder = true)
public class AuthRequest {
    private String username;
    private String password;
}