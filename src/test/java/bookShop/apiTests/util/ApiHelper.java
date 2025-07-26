package bookShop.apiTests.util;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import bookShop.apiTests.model.AuthRequest;

public class ApiHelper {

    private final String ADMIN_LOGIN = System.getenv("TEST_ADMIN_USERNAME");
    private final String ADMIN_PASSWORD = System.getenv("TEST_ADMIN_PASSWORD");

    public Response post(String path, Object body) {
        return RestAssured.given()
                .contentType("application/json")
                .body(body)
                .post(path)
                .then()
                .extract().response();
    }

    public Response deleteUser(String userId, String adminToken) {
        return RestAssured.given()
                .header("Authorization", "Bearer " + adminToken)
                .delete("/users/id/" + userId)
                .then()
                .extract().response();
    }

    public String getAdminToken() {
        AuthRequest loginReq = AuthRequest.builder()
                .username(ADMIN_LOGIN)
                .password(ADMIN_PASSWORD)
                .build();
        Response resp = post("/auth/login", loginReq);
        return resp.jsonPath().getString("data.token");
    }
}
