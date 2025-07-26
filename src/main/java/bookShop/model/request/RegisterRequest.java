package bookShop.model.request;

import lombok.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;
import bookShop.validation.ValidPassword;
import javax.validation.constraints.Pattern;
import bookShop.model.Role;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import bookShop.validation.NoEmojiNoXss;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonDeserialize(using = TrimmingRegisterRequestDeserializer.class)
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
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Email должен быть в формате name@domain.zone"
    )
    @Size(max = 255, message = "Email не должен превышать 255 символов")
    private String email;
}
