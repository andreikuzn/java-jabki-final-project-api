package bookShop.apiTests.util;

import io.restassured.response.Response;
import bookShop.apiTests.model.RegisterRequest;
import bookShop.apiTests.model.AuthRequest;
import bookShop.apiTests.common.TestDataUtil;

import static bookShop.model.Role.USER;

public class UserTestUtil {
    private final ApiHelper apiHelper;

    public UserTestUtil(ApiHelper apiHelper) {
        this.apiHelper = apiHelper;
    }

    public RegisterRequest generateRandomUser() {
        return RegisterRequest.builder()
                .username(TestDataUtil.randomUsername())
                .password(TestDataUtil.validPassword())
                .role(USER.getEnName())
                .phone(TestDataUtil.randomPhone())
                .email(TestDataUtil.randomEmail())
                .build();
    }

    public String registerUser(RegisterRequest request) {
        Response resp = apiHelper.post("/auth/register", request);
        return resp.jsonPath().getString("data.id");
    }

    public String loginAndGetToken(String login, String password) {
        AuthRequest req = AuthRequest.builder()
                .username(login)
                .password(password)
                .build();
        Response resp = apiHelper.post("/auth/login", req);
        return resp.jsonPath().getString("data.token");
    }

    public void deleteUser(String userId) {
        String adminToken = apiHelper.getAdminToken();
        Response resp = apiHelper.deleteUser(userId, adminToken);
        if (resp.getStatusCode() != 200 && resp.getStatusCode() != 404) {
            throw new RuntimeException("Ошибка при удалении пользователя: " + resp.asString());
        }
    }
}