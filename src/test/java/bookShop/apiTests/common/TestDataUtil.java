package bookShop.apiTests.common;

import java.util.Random;

public class TestDataUtil {
    private static final Random RANDOM = new Random();

    public static String randomSuffix() {
        return System.currentTimeMillis() + "_" + (1000 + RANDOM.nextInt(9000));
    }

    public static String randomUsername() {
        return "user_" + randomSuffix();
    }

    public static String randomAdminUsername() {
        return "admin_test_" + randomSuffix();
    }

    public static String validPassword() {
        return "TestPass" + randomSuffix() + "!";
    }

    public static String randomEmail() {
        return randomUsername() + "@mail.com";
    }

    public static String randomPhone() {
        return "7999" + (1000000 + RANDOM.nextInt(9000000));
    }
}
