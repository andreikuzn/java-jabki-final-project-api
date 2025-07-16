package bookShop.model;

import lombok.*;

@Data
@AllArgsConstructor
public class AuthFullResponse {
    private String token;
    private Long userId;
    private Role role;
}