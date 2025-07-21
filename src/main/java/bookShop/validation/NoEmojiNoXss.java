package bookShop.validation;

import javax.validation.Constraint;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import javax.validation.Payload;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = NoEmojiNoXssValidator.class)
@Target({FIELD})
@Retention(RUNTIME)
public @interface NoEmojiNoXss {
    String message() default "Поле содержит запрещённые символы (emoji или XSS)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}