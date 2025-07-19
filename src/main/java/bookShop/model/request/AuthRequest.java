package bookShop.model.request;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
    private String username;
    private String password;

    public void trimFields() {
        if (username != null) username = username.trim();
        if (password != null) password = password.trim();
    }
}