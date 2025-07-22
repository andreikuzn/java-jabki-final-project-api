package bookShop.apiTests.util;

import bookShop.apiTests.auth.dto.RegisterRequest;
import java.util.Random;

public class UserTestUtil {

    public static String randomSuffix() {
        return System.currentTimeMillis() + "_" + (1000 + new Random().nextInt(9000));
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
        return "7999" + (1000000 + new Random().nextInt(9000000));
    }

    public static RegisterRequest userRegisterRequest(String username, String password) {
        RegisterRequest req = new RegisterRequest();
        req.setUsername(username);
        req.setPassword(password);
        req.setRole("USER");
        req.setPhone(randomPhone());
        req.setEmail(randomEmail());
        return req;
    }

    public static RegisterRequest adminRegisterRequest(String username, String password) {
        RegisterRequest req = new RegisterRequest();
        req.setUsername(username);
        req.setPassword(password);
        req.setRole("ADMIN");
        req.setPhone(randomPhone());
        req.setEmail(randomEmail());
        return req;
    }

    public static RegisterRequest defaultRegisterRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername(randomUsername());
        req.setPassword(validPassword());
        req.setRole("USER");
        req.setPhone(randomPhone());
        req.setEmail(randomEmail());
        return req;
    }
}