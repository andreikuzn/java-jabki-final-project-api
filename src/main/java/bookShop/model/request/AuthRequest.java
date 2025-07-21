package bookShop.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import bookShop.validation.NoEmojiNoXss;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
    @NotBlank(message = "Имя пользователя не должно быть пустым")
    @Size(min = 2, max = 32, message = "Имя пользователя должно быть от 2 до 32 символов")
    @NoEmojiNoXss
    private String username;
    @NotBlank(message = "Пароль не должен быть пустым")
    @Size(min = 6, max = 64, message = "Пароль должен быть от 6 до 64 символов")
    @NoEmojiNoXss
    private String password;

    public void trimFields() {
        if (username != null) username = username.trim();
        if (password != null) password = password.trim();
    }
}