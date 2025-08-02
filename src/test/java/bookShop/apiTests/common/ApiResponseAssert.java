package bookShop.apiTests.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import bookShop.apiTests.model.RegisterRequest;
import bookShop.apiTests.enums.ApiError;

import static bookShop.apiTests.enums.ErrorMessage.METHOD_NOT_ALLOWED;
import static bookShop.apiTests.enums.ErrorMessage.UNSUPPORTED_MEDIA_TYPE;
import static bookShop.apiTests.enums.ErrorMessage.USER_ALREADY_EXISTS;
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
            assertNotNull(data, "data должен быть не null");
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

    public static void assertRegisterSuccess(Response resp, RegisterRequest expected) {
        assertEquals(200, resp.getStatusCode());
        try {
            ApiResponse<UserData> apiResp = objectMapper.readValue(
                    resp.asString(),
                    objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, UserData.class)
            );
            assertNull(apiResp.getError());
            assertEquals(200, apiResp.getStatus());
            assertNotNull(apiResp.getTimestamp());
            UserData user = apiResp.getData();
            assertNotNull(user, "data должен быть не null");
            assertEquals(expected.getUsername(), user.getUsername());
            assertEquals(expected.getRole(), user.getRole());
            assertEquals(expected.getPhone(), user.getPhone());
            assertEquals(expected.getEmail(), user.getEmail());
            assertEquals(0, user.getLoyaltyPoints());
            assertEquals("Новичок", user.getLoyaltyLevel());
            assertNotNull(user.getActiveLoans());
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
    public static void assertInvalidCredentials(Response resp, String message) {
        assertEquals(ApiError.INVALID_CREDENTIALS.getStatus(), resp.getStatusCode());
        try {
            ApiResponse<?> apiResp = objectMapper.readValue(resp.asString(), ApiResponse.class);
            assertEquals(ApiError.INVALID_CREDENTIALS.getCode(), apiResp.getError());
            assertTrue(apiResp.getMessage().contains(message),
                    "Сообщение должно содержать текст про неверный логин или пароль");
            assertEquals(ApiError.INVALID_CREDENTIALS.getStatus(), apiResp.getStatus());
            assertNotNull(apiResp.getTimestamp(), "Поле Timestamp не должно быть пустым");
            assertNull(apiResp.getData(), "Поле Data должно быть пустым");
        } catch (Exception e) {
            fail("Ошибка парсинга или проверки: " + e.getMessage());
        }
    }

    public static void assertUserNotFound(Response resp, String message) {
        assertEquals(ApiError.USER_NOT_FOUND.getStatus(), resp.getStatusCode());
        try {
            ApiResponse<?> apiResp = objectMapper.readValue(resp.asString(), ApiResponse.class);
            assertEquals(ApiError.USER_NOT_FOUND.getCode(), apiResp.getError());
            assertTrue(apiResp.getMessage().contains(message),
                    "Сообщение должно содержать текст про пользователя не найдено");
            assertEquals(ApiError.USER_NOT_FOUND.getStatus(), apiResp.getStatus());
            assertNotNull(apiResp.getTimestamp(), "Поле Timestamp не должно быть пустым");
            assertNull(apiResp.getData(), "Поле Data должно быть пустым");
        } catch (Exception e) {
            fail("Ошибка парсинга или проверки: " + e.getMessage());
        }
    }

    public static void assertBadRequest(Response resp, String message) {
        assertEquals(ApiError.BAD_REQUEST.getStatus(), resp.getStatusCode());
        try {
            ApiResponse<?> apiResp = objectMapper.readValue(resp.asString(), ApiResponse.class);
            assertEquals(ApiError.BAD_REQUEST.getCode(), apiResp.getError());
            assertTrue(apiResp.getMessage().contains(message), "Сообщение не должно быть пустым");
            assertEquals(ApiError.BAD_REQUEST.getStatus(), apiResp.getStatus());
            assertNotNull(apiResp.getTimestamp(), "Поле Timestamp не должно быть пустым");
            assertNull(apiResp.getData(), "Поле Data должно быть пустым");
        } catch (Exception e) {
            fail("Ошибка парсинга или проверки: " + e.getMessage());
        }
    }

    public static void assertUserAlreadyExists(Response resp) {
        assertEquals(ApiError.USER_ALREADY_EXISTS.getStatus(), resp.getStatusCode());
        try {
            ApiResponse<?> apiResp = objectMapper.readValue(resp.asString(), ApiResponse.class);
            assertEquals(ApiError.USER_ALREADY_EXISTS.getCode(), apiResp.getError());
            assertEquals(USER_ALREADY_EXISTS.getMsg(), apiResp.getMessage());
            assertEquals(ApiError.USER_ALREADY_EXISTS.getStatus(), apiResp.getStatus());
            assertNotNull(apiResp.getTimestamp(), "Поле Timestamp не должно быть пустым");
            assertNull(apiResp.getData(), "Поле Data должно быть пустым");
        } catch (Exception e) {
            fail("Ошибка парсинга или проверки: " + e.getMessage());
        }
    }

    public static void assertUnsupportedMediaType(Response resp) {
        assertEquals(ApiError.UNSUPPORTED_MEDIA_TYPE.getStatus(), resp.getStatusCode());
        try {
            ApiResponse<?> apiResp = objectMapper.readValue(resp.asString(), ApiResponse.class);
            assertEquals(ApiError.UNSUPPORTED_MEDIA_TYPE.getCode(), apiResp.getError());
            assertEquals(UNSUPPORTED_MEDIA_TYPE.getMsg(), apiResp.getMessage());
            assertEquals(ApiError.UNSUPPORTED_MEDIA_TYPE.getStatus(), apiResp.getStatus());
            assertNotNull(apiResp.getTimestamp(), "Поле Timestamp не должно быть пустым");
            assertNull(apiResp.getData(), "Поле Data должно быть пустым");
        } catch (Exception e) {
            fail("Ошибка парсинга или проверки: " + e.getMessage());
        }
    }

    public static void assertMethodNotAllowed(Response resp, String method) {
        assertEquals(ApiError.METHOD_NOT_ALLOWED.getStatus(), resp.getStatusCode());
        try {
            ApiResponse<?> apiResp = objectMapper.readValue(resp.asString(), ApiResponse.class);
            assertEquals(ApiError.METHOD_NOT_ALLOWED.getCode(), apiResp.getError());
            assertEquals(String.format(METHOD_NOT_ALLOWED.getMsg(), method), apiResp.getMessage());
            assertEquals(ApiError.METHOD_NOT_ALLOWED.getStatus(), apiResp.getStatus());
            assertNotNull(apiResp.getTimestamp(), "Поле Timestamp не должно быть пустым");
            assertNull(apiResp.getData(), "Поле Data должно быть пустым");
        } catch (Exception e) {
            fail("Ошибка парсинга или проверки: " + e.getMessage());
        }
    }

    public static void assertForbidden(Response resp, String message, String error) {
        assertEquals(ApiError.FORBIDDEN.getStatus(), resp.getStatusCode());
        try {
            ApiResponse<?> apiResp = objectMapper.readValue(resp.asString(), ApiResponse.class);
            assertEquals(error, apiResp.getError());
            assertTrue(apiResp.getMessage().contains(message), "Сообщение не должно быть пустым");
            assertEquals(ApiError.FORBIDDEN.getStatus(), apiResp.getStatus());
            assertNotNull(apiResp.getTimestamp(), "Поле Timestamp не должно быть пустым");
            assertNull(apiResp.getData(), "Поле Data должно быть пустым");
        } catch (Exception e) {
            fail("Ошибка парсинга или проверки: " + e.getMessage());
        }
    }
}