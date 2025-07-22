package bookShop.model;

import lombok.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;
import bookShop.validation.ValidPassword;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Email;
import bookShop.validation.NoEmojiNoXss;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Имя пользователя не должно быть пустым")
    @Size(min = 2, max = 32, message = "Имя пользователя должно быть от 2 до 32 символов")
    @NoEmojiNoXss
    private String username;

    @ValidPassword
    @NoEmojiNoXss
    private String password;

    @NotNull(message = "Роль пользователя обязательна")
    private Role role;

    @NotBlank(message = "Поле телефон пользователя не должно быть пустым")
    @Pattern(regexp = "\\d{11}", message = "Телефон должен состоять ровно из 11 цифр")
    private String phone;

    @NotBlank(message = "Поле email пользователя не должно быть пустым")
    @Email(message = "Некорректный email")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Некорректный email"
    )
    private String email;

    public void trimFields() {
        if (username != null) username = username.trim();
        if (password != null) password = password.trim();
        if (phone != null) phone = phone.trim();
        if (email != null) email = email.trim();
    }
}
