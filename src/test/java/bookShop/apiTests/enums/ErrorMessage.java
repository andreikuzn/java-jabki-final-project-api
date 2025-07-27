package bookShop.apiTests.enums;

import lombok.*;

@Getter
@RequiredArgsConstructor
public enum ErrorMessage {
    USERNAME_EMPTY("username: Имя пользователя не должно быть пустым; "),
    PASSWORD_EMPTY("password: Пароль не должен быть пустым; "),
    USERNAME_LENGTH("username: Имя пользователя должно быть от 2 до 32 символов; "),
    PASSWORD_LENGTH("password: Пароль должен быть от 6 до 64 символов"),
    INVALID_OR_EMPTY_JSON("Некорректный или пустой JSON"),
    USER_ALREADY_EXISTS("Пользователь с таким именем уже существует"),
    ADMIN_LIMIT("В системе не может быть более 3-х админов"),
    PHONE_EMPTY("phone: Поле телефон пользователя не должно быть пустым; "),
    PHONE_INVALID("phone: Телефон должен состоять ровно из 11 цифр; "),
    EMAIL_EMPTY("email: Поле email пользователя не должно быть пустым; "),
    EMAIL_FORMAT("email: Email должен быть в формате name@domain.zone; "),
    EMAIL_LENGTH("email: Email не должен превышать 255 символов; "),
    FORBIDDEN_CHARS("Поле содержит запрещённые символы (emoji или XSS)"),
    METHOD_NOT_ALLOWED("Метод %s не поддерживается для этого ресурса"),
    ENDPOINT_NOT_FOUND("Эндпоинт не найден"),
    UNSUPPORTED_MEDIA_TYPE("Неподдерживаемый Content-Type"),
    USER_NOT_FOUND("Пользователь не найден"),
    INVALID_CREDENTIALS("Неверный логин или пароль"),
    ROLE_EMPTY("role: Роль пользователя обязательна; ");

    private final String msg;
}