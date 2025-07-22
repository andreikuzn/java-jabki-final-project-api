package bookShop.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    String message() default "Пароль должен быть от 6 до 64 символов, только латинские буквы, минимум одна заглавная буква, один спецсимвол и одна цифра";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}