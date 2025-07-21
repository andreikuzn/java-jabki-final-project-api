package bookShop.validation;

import javax.validation.ConstraintValidator;
import java.util.regex.Pattern;

public class NoEmojiNoXssValidator implements ConstraintValidator<NoEmojiNoXss, String> {
    private static final Pattern XSS_PATTERN = Pattern.compile("[<>\"'&]");
    private static final Pattern EMOJI_PATTERN = Pattern.compile("[\\p{So}\\p{Cn}]");

    @Override
    public boolean isValid(String value, javax.validation.ConstraintValidatorContext context) {
        if (value == null) return true;
        if (XSS_PATTERN.matcher(value).find()) return false;
        if (EMOJI_PATTERN.matcher(value).find()) return false;
        return true;
    }
}