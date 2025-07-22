package bookShop.validation;

import javax.validation.ConstraintValidator;
import java.util.regex.Pattern;

public class NoEmojiNoXssValidator implements ConstraintValidator<NoEmojiNoXss, String> {
    private static final Pattern XSS_PATTERN = Pattern.compile("[<>\"'&]");
    private static final Pattern EMOJI_PATTERN = Pattern.compile(
            "[\\p{So}\\p{Cn}" +
                    "\\x{203C}-\\x{3299}" +
                    "\\x{1F000}-\\x{1F9FF}" +     // Основная emoji зона
                    "\\x{1FA70}-\\x{1FAFF}" +     // Дополнительная emoji зона
                    "\\x{1F1E6}-\\x{1F1FF}" +     // Флаги
                    "\\x{1F201}-\\x{1F251}" +
                    "\\x{1F600}-\\x{1F64F}" +     // Смайлы
                    "\\x{1F300}-\\x{1F5FF}" +     // Символы/иконки
                    "\\x{1F680}-\\x{1F6FF}" +     // Транспорт
                    "\\x{1F900}-\\x{1F9FF}" +     // Доп. эмодзи
                    "\\x{2600}-\\x{26FF}" +       // Символы Misc
                    "\\x{2700}-\\x{27BF}" +       // Символы Dingbats
                    "]"
    );

    @Override
    public boolean isValid(String value, javax.validation.ConstraintValidatorContext context) {
        if (value == null) return true;
        if (XSS_PATTERN.matcher(value).find()) return false;
        if (EMOJI_PATTERN.matcher(value).find()) return false;
        return true;
    }
}