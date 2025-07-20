package bookShop.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) return false;
        password = password.trim();
        if (password.length() < 6 || password.length() > 64) return false;
        if (!password.matches("^[A-Za-z\\d!@#$%^&*()_+\\-={}:;\"'<>,.?\\[\\]\\\\|`~/.]+$")) return false;
        if (!password.matches(".*[A-Z].*")) return false;
        if (!password.matches(".*[!@#$%^&*()_+\\-={}:;\"'<>,.?\\[\\]\\\\|`~/.].*")) return false;
        return true;
    }
}