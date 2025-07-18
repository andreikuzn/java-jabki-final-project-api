package bookShop.model;

import lombok.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;
import bookShop.validation.ValidPassword;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Имя пользователя не должно быть пустым")
    @Size(min = 2, max = 32, message = "Имя пользователя должно быть от 2 до 32 символов")
    private String username;

    @ValidPassword
    private String password;

    @NotNull(message = "Роль пользователя обязательна")
    private Role role;

    public void trimFields() {
        if (username != null) username = username.trim();
        if (password != null) password = password.trim();
    }
}