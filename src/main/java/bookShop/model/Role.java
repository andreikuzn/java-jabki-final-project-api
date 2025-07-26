package bookShop.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    USER("Пользователь", "USER"),
    ADMIN("Администратор", "ADMIN");

    private final String ruName;
    private final String enName;
}

