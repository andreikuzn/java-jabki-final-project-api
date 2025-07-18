package bookShop.model;

import lombok.Data;

import lombok.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Имя пользователя не должно быть пустым")
    @Size(min = 2, max = 32, message = "Имя пользователя должно быть от 2 до 32 символов")
    private String username;
    @NotBlank(message = "Пароль не должен быть пустым")
    @Size(min = 6, max = 64, message = "Пароль должен быть от 8 до 64 символов")
    private String password;
    @NotNull(message = "Роль пользователя обязательна")
    private Role role;
}
