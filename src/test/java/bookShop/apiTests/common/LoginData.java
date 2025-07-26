package bookShop.apiTests.common;

import lombok.Data;

@Data
public class LoginData {
    private String token;
    private int userId;
    private String role;
}