package bookShop.apiTests.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import static org.junit.jupiter.api.Assertions.*;

public class ApiResponseAssert {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void assertRegisterSuccess(
            Response resp,
            String expectedEmail,
            String expectedUsername,
            String expectedRole,
            String expectedPhone
    ) {
        assertEquals(200, resp.getStatusCode());
        try {
            ApiResponse<UserData> apiResp = objectMapper.readValue(
                    resp.asString(),
                    objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, UserData.class)
            );
            assertNull(apiResp.getError());
            assertEquals(200, apiResp.getStatus());
            assertEquals("Пользователь успешно зарегистрирован", apiResp.getMessage());
            assertNotNull(apiResp.getTimestamp());
            UserData data = apiResp.getData();
            assertNotNull(data);
            assertEquals(expectedEmail, data.getEmail());
            assertEquals(expectedUsername, data.getUsername());
            assertEquals(expectedRole, data.getRole());
            assertEquals(expectedPhone, data.getPhone());
            assertEquals(0, data.getLoyaltyPoints());
            assertEquals("Новичок", data.getLoyaltyLevel());
            assertNotNull(data.getActiveLoans());
        } catch (Exception e) {
            fail("Ошибка парсинга или проверки: " + e.getMessage());
        }
    }

    public static void assertAuthSuccess(Response resp, int expectedUserId, String expectedRole) {
        assertEquals(200, resp.getStatusCode());
        try {
            ApiResponse<LoginData> apiResp = objectMapper.readValue(
                    resp.asString(),
                    objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, LoginData.class)
            );
            assertNull(apiResp.getError());
            assertEquals(200, apiResp.getStatus());
            assertNull(apiResp.getMessage());
            assertNotNull(apiResp.getTimestamp());
            LoginData data = apiResp.getData();
            assertNotNull(data, "data должен быть не null");
            assertNotNull(data.getToken(), "token должен быть не null");
            assertEquals(expectedUserId, data.getUserId(), "userId не совпадает");
            assertEquals(expectedRole, data.getRole(), "role не совпадает");
        } catch (Exception e) {
            fail("Ошибка парсинга или проверки: " + e.getMessage());
        }
    }

    public static void assertErrorPartly(Response resp, int expectedStatus, String expectedError, String expectedMessage) {
        assertEquals(expectedStatus, resp.getStatusCode());
        try {
            ApiResponse<?> apiResp = objectMapper.readValue(resp.asString(), ApiResponse.class);
            assertEquals(expectedError, apiResp.getError());
            assertTrue(apiResp.getMessage().contains(expectedMessage),
                    "Ожидалось, что message будет включать: " + expectedMessage + ", но вернулось: " + apiResp.getMessage());
            assertEquals(expectedStatus, apiResp.getStatus());
            assertNotNull(apiResp.getTimestamp());
            assertNull(apiResp.getData());
        } catch (Exception e) {
            fail("Ошибка парсинга или проверки: " + e.getMessage());
        }
    }

    public static void assertError(Response resp, int expectedStatus, String expectedError, String expectedMessage) {
        assertEquals(expectedStatus, resp.getStatusCode());
        try {
            ApiResponse<?> apiResp = objectMapper.readValue(resp.asString(), ApiResponse.class);
            assertEquals(expectedError, apiResp.getError());
            assertEquals(expectedMessage, apiResp.getMessage());
            assertEquals(expectedStatus, apiResp.getStatus());
            assertNotNull(apiResp.getTimestamp());
            assertNull(apiResp.getData());
        } catch (Exception e) {
            fail("Ошибка парсинга или проверки: " + e.getMessage());
        }
    }
    public static void assertAuthInvalidCredentials(Response resp) {
        assertEquals(401, resp.getStatusCode());
        try {
            ApiResponse<?> apiResp = objectMapper.readValue(resp.asString(), ApiResponse.class);
            assertEquals("INVALID_CREDENTIALS", apiResp.getError());
            assertTrue(apiResp.getMessage().toLowerCase().contains("логин или пароль"),
                    "Сообщение должно содержать текст про неверный логин или пароль");
            assertEquals(401, apiResp.getStatus());
            assertNotNull(apiResp.getTimestamp());
            assertNull(apiResp.getData());
        } catch (Exception e) {
            fail("Ошибка парсинга или проверки: " + e.getMessage());
        }
    }

    public static void assertAuthUserNotFound(Response resp) {
        assertEquals(404, resp.getStatusCode());
        try {
            ApiResponse<?> apiResp = objectMapper.readValue(resp.asString(), ApiResponse.class);
            assertEquals("USER_NOT_FOUND", apiResp.getError());
            assertTrue(apiResp.getMessage().toLowerCase().contains("не найден"),
                    "Сообщение должно содержать текст про пользователя не найдено");
            assertEquals(404, apiResp.getStatus());
            assertNotNull(apiResp.getTimestamp());
            assertNull(apiResp.getData());
        } catch (Exception e) {
            fail("Ошибка парсинга или проверки: " + e.getMessage());
        }
    }
}