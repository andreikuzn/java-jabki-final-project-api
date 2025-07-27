package bookShop.apiTests.tests.auth;

import bookShop.apiTests.model.AuthRequest;
import bookShop.apiTests.common.ApiResponseAssert;
import bookShop.apiTests.common.TestDataUtil;
import bookShop.apiTests.common.BaseIntegrationTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import bookShop.repository.AppUserRepository;
import bookShop.apiTests.model.RegisterRequest;
import  bookShop.apiTests.common.DbResponseAssert;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import bookShop.apiTests.enums.ApiError;
import bookShop.apiTests.enums.ApiHeader;

import java.util.HashMap;
import java.util.Map;

import static bookShop.apiTests.enums.ApiError.METHOD_NOT_ALLOWED;
import static bookShop.apiTests.enums.ApiError.NOT_FOUND;
import static bookShop.apiTests.enums.ApiError.OK;
import static bookShop.apiTests.enums.ApiError.UNSUPPORTED_MEDIA_TYPE;
import static bookShop.apiTests.enums.ApiPath.AUTH_LOGIN;
import static bookShop.apiTests.enums.ApiPath.AUTH_NOT_EXIST;
import static bookShop.apiTests.enums.ApiPath.AUTH_REGISTER;
import static bookShop.apiTests.enums.ErrorMessage.PASSWORD_EMPTY;
import static bookShop.apiTests.enums.ErrorMessage.PASSWORD_LENGTH;
import static bookShop.apiTests.enums.ErrorMessage.USERNAME_EMPTY;
import static bookShop.apiTests.enums.ErrorMessage.ENDPOINT_NOT_FOUND;
import static bookShop.apiTests.enums.ErrorMessage.FORBIDDEN_CHARS;
import static bookShop.apiTests.enums.ErrorMessage.INVALID_CREDENTIALS;
import static bookShop.apiTests.enums.ErrorMessage.INVALID_OR_EMPTY_JSON;
import static bookShop.apiTests.enums.ErrorMessage.USERNAME_LENGTH;
import static bookShop.apiTests.enums.ErrorMessage.USER_NOT_FOUND;
import static bookShop.apiTests.enums.JsonPathKey.DATA_ID;
import static bookShop.model.Role.ADMIN;
import static bookShop.model.Role.USER;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@DisplayName("Auth /login: all fields headers positive/negative")
public class AuthLoginValidationIT extends BaseIntegrationTest {

    @Autowired
    private AppUserRepository appUserRepository;

    // ====== Валидация username ======

    static Stream<String> invalidUsernames() {
        return Stream.of("", " ", null);
    }

    static Stream<String> badPasswords() {
        return Stream.of(
                "user😀",
                "<script>alert(1)</script>",
                "user' OR 1=1--"
        );
    }

    static Stream<String> badUsernames() {
        return Stream.of(
                "user😀",
                "<script>alert(1)</script>",
                "user' OR 1=1--"
        );
    }

    static Stream<String> invalidBodies() {
        return Stream.of(
                "",
                "null",
                "[{\"username\": \"user\", \"password\": \"A1b!1234\"}]",
                "12345",
                "\"string\"",
                "{username: test, password: }"
        );
    }

    static Stream<String> invalidPasswords() {
        return Stream.of(
                "",
                " ",
                null
        );
    }

    @ParameterizedTest(name = "Login: username = \"{0}\" (пусто/пробел/null)")
    @MethodSource("invalidUsernames")
    void loginWithInvalidUsername(String username) {
        AuthRequest request = AuthRequest.builder()
                .username(username)
                .password(TestDataUtil.validPassword())
                .build();
        Response response = apiHelper.post(AUTH_LOGIN.getPath(), request);
        ApiResponseAssert.assertBadRequest(response, USERNAME_EMPTY.getMsg());
    }

    @Test
    @DisplayName("Login: username = int вместо строки")
    void loginWithIntUsername() {
        String password = TestDataUtil.validPassword();
        String email = TestDataUtil.randomEmail();
        String phone = TestDataUtil.randomPhone();
        Integer username = 123456789;
        String shouldBeIgnored = "shouldBeIgnored";
        Map<String, Object> requestReg = new HashMap<>();
        requestReg.put("username", username);
        requestReg.put("password", password);
        requestReg.put("role", USER);
        requestReg.put("phone", phone);
        requestReg.put("email", email);
        requestReg.put("extraField", shouldBeIgnored);
        String userId = null;
        try {
            Response responseReg = apiHelper.post(AUTH_REGISTER.getPath(), requestReg);
            ApiResponseAssert.assertRegisterSuccess(
                    responseReg,
                    email,
                    String.valueOf(username),
                    USER.name(),
                    phone
            );
            userId = responseReg.jsonPath().getString(DATA_ID.getPath());
            Map<String, Object> loginMap = new HashMap<>();
            loginMap.put("username", username);
            loginMap.put("password", password);
            Response responseAuth = apiHelper.post(AUTH_LOGIN.getPath(), loginMap);
            ApiResponseAssert.assertAuthSuccess(
                    responseAuth,
                    Integer.parseInt(String.valueOf(userId)),
                    USER.getEnName());
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Login: username = 1 символ")
    void loginWithUsername1Symbol() {
        AuthRequest request = AuthRequest.builder()
                .username("a")
                .build();
        Response response = apiHelper.post(AUTH_LOGIN.getPath(), request);
        ApiResponseAssert.assertBadRequest(response, USERNAME_LENGTH.getMsg());
    }

    @Test
    @DisplayName("Login: username = 2 символа (валид)")
    void loginWithUsername2Symbols() {
        RegisterRequest requestReg = userTestUtil.generateRandomUser().toBuilder()
                .username("us")
                .build();
        String userId = null;
        try {
            Response responseReg = apiHelper.post(AUTH_REGISTER.getPath(), requestReg);
            ApiResponseAssert.assertRegisterSuccess(responseReg, requestReg);
            userId = responseReg.jsonPath().getString(DATA_ID.getPath());
            AuthRequest request = AuthRequest.builder()
                    .username(requestReg.getUsername())
                    .password(requestReg.getPassword())
                    .build();
            Response response = apiHelper.post(AUTH_LOGIN.getPath(), request);
            ApiResponseAssert.assertAuthSuccess(response, Integer.parseInt(userId), USER.getEnName());
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Login: username = 32 символа (валид)")
    void loginWithUsername32Symbols() {
        RegisterRequest requestReg = userTestUtil.generateRandomUser().toBuilder()
                .username("b".repeat(32))
                .build();
        String userId = null;
        try {
            Response responseReg = apiHelper.post(AUTH_REGISTER.getPath(), requestReg);
            ApiResponseAssert.assertRegisterSuccess(responseReg, requestReg);
            userId = responseReg.jsonPath().getString(DATA_ID.getPath());
            AuthRequest request = AuthRequest.builder()
                    .username(requestReg.getUsername())
                    .password(requestReg.getPassword())
                    .build();
            Response response = apiHelper.post(AUTH_LOGIN.getPath(), request);
            ApiResponseAssert.assertAuthSuccess(response, Integer.parseInt(userId), USER.getEnName());
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Login: username = 33 символа")
    void loginWithUsername33Symbols() {
        AuthRequest request = AuthRequest.builder()
                .username("b".repeat(33))
                .build();
        Response response = apiHelper.post(AUTH_LOGIN.getPath(), request);
        ApiResponseAssert.assertBadRequest(response, USERNAME_LENGTH.getMsg());
    }

    @ParameterizedTest(name = "Login: username = \"{0}\" (emoji/XSS/SQL)")
    @MethodSource("badUsernames")
    void loginWithBadUsernames(String username) {
        AuthRequest request = AuthRequest.builder()
                .username(username)
                .password(TestDataUtil.validPassword())
                .build();
        Response response = apiHelper.post(AUTH_LOGIN.getPath(), request);
        ApiResponseAssert.assertBadRequest(response, FORBIDDEN_CHARS.getMsg());
    }

    // ====== Валидация password ======

    @ParameterizedTest(name = "Login: password = \"{0}\" (empty/null/space)")
    @MethodSource("invalidPasswords")
    void loginWithInvalidPassword(String password) {
        AuthRequest request = AuthRequest.builder()
                .username(TestDataUtil.randomUsername())
                .password(password)
                .build();
        Response response = apiHelper.post(AUTH_LOGIN.getPath(), request);
        ApiResponseAssert.assertBadRequest(response, PASSWORD_EMPTY.getMsg());
    }

    @Test
    @DisplayName("Login: password = int вместо строки")
    void loginWithIntPassword() {
        Map<String, Object> loginMap = new HashMap<>();
        loginMap.put("username", TestDataUtil.randomUsername());
        loginMap.put("password", 123456789);
        Response responseAuth = apiHelper.post(AUTH_LOGIN.getPath(), loginMap);
        ApiResponseAssert.assertUserNotFound(responseAuth, USER_NOT_FOUND.getMsg());
    }

    @Test
    @DisplayName("Login: password = 5 символов (невалид)")
    void loginWithPassword5Symbols() {
        AuthRequest request = AuthRequest.builder()
                .username(TestDataUtil.randomUsername())
                .password("A1b!2")
                .build();
        Response response = apiHelper.post(AUTH_LOGIN.getPath(), request);
        ApiResponseAssert.assertBadRequest(response, PASSWORD_LENGTH.getMsg());
    }

    @Test
    @DisplayName("Login: password = 6 символов (валид)")
    void loginWithPassword6Symbols() {
        RegisterRequest requestReg = userTestUtil.generateRandomUser().toBuilder()
                .username(TestDataUtil.randomUsername())
                .password("TeDa@2")
                .build();
        String userId = null;
        try {
            Response responseReg = apiHelper.post(AUTH_REGISTER.getPath(), requestReg);
            ApiResponseAssert.assertRegisterSuccess(responseReg, requestReg);
            userId = responseReg.jsonPath().getString(DATA_ID.getPath());
            AuthRequest request = AuthRequest.builder()
                    .username(requestReg.getUsername())
                    .password(requestReg.getPassword())
                    .build();
            Response response = apiHelper.post(AUTH_LOGIN.getPath(), request);
            ApiResponseAssert.assertAuthSuccess(response, Integer.parseInt(userId), USER.getEnName());
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Login: password = 64 символа (валид)")
    void loginWithPassword64Symbols() {
        RegisterRequest requestReg = userTestUtil.generateRandomUser().toBuilder()
                .username(TestDataUtil.randomUsername())
                .password("A1b@" + "a".repeat(60))
                .build();
        String userId = null;
        try {
            Response responseReg = apiHelper.post(AUTH_REGISTER.getPath(), requestReg);
            ApiResponseAssert.assertRegisterSuccess(responseReg, requestReg);
            userId = responseReg.jsonPath().getString(DATA_ID.getPath());
            AuthRequest request = AuthRequest.builder()
                    .username(requestReg.getUsername())
                    .password(requestReg.getPassword())
                    .build();
            Response response = apiHelper.post(AUTH_LOGIN.getPath(), request);
            ApiResponseAssert.assertAuthSuccess(response, Integer.parseInt(userId), USER.getEnName());
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Login: password = 65 символов (невалид)")
    void loginWithPassword65Symbols() {
        AuthRequest request = AuthRequest.builder()
                .username(TestDataUtil.randomUsername())
                .password("A1b!" + "a".repeat(61))
                .build();
        Response response = apiHelper.post(AUTH_LOGIN.getPath(), request);
        ApiResponseAssert.assertBadRequest(response, PASSWORD_LENGTH.getMsg());
    }

    @Test
    @DisplayName("Login: password с пробелом посередине")
    void loginWithPasswordSpaceMiddle() {
        AuthRequest request = AuthRequest.builder()
                .username(TestDataUtil.randomUsername())
                .password("Test pass1!")
                .build();
        Response response = apiHelper.post(AUTH_LOGIN.getPath(), request);
        ApiResponseAssert.assertUserNotFound(response, USER_NOT_FOUND.getMsg());
    }

    @ParameterizedTest(name = "Login: username = {0} (emoji/XSS/SQL)")
    @MethodSource("badPasswords")
    void loginWithDangerousUsername(String password) {
        AuthRequest request = AuthRequest.builder()
                .username(TestDataUtil.randomUsername())
                .password(password)
                .build();
        Response response = apiHelper.post(AUTH_LOGIN.getPath(), request);
        ApiResponseAssert.assertBadRequest(response, FORBIDDEN_CHARS.getMsg());
    }

    // ====== Позитивные сценарии ======

    @Test
    @DisplayName("Login: Happy path USER")
    void loginHappyPathUser() {
        RegisterRequest requestReg = userTestUtil.generateRandomUser();
        String userId = null;
        try {
            Response responseReg = apiHelper.post(AUTH_REGISTER.getPath(), requestReg);
            ApiResponseAssert.assertRegisterSuccess(responseReg, requestReg);
            userId = responseReg.jsonPath().getString(DATA_ID.getPath());
            AuthRequest request = AuthRequest.builder()
                    .username(requestReg.getUsername())
                    .password(requestReg.getPassword())
                    .build();
            Response response = apiHelper.post(AUTH_LOGIN.getPath(), request);
            ApiResponseAssert.assertAuthSuccess(response, Integer.parseInt(userId), USER.getEnName());
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Login: Happy path ADMIN")
    void loginHappyPathAdmin() {
        RegisterRequest requestReg = userTestUtil.generateRandomUser().toBuilder()
                .role(ADMIN.getEnName())
                .build();
        String userId = null;
        try {
            Response responseReg = apiHelper.post(AUTH_REGISTER.getPath(), requestReg);
            ApiResponseAssert.assertRegisterSuccess(responseReg, requestReg);
            userId = responseReg.jsonPath().getString(DATA_ID.getPath());
            AuthRequest request = AuthRequest.builder()
                    .username(requestReg.getUsername())
                    .password(requestReg.getPassword())
                    .build();
            Response response = apiHelper.post(AUTH_LOGIN.getPath(), request);
            ApiResponseAssert.assertAuthSuccess(response, Integer.parseInt(userId), ADMIN.getEnName());
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Login: username и password с пробелами по краям")
    void loginWithTrimmedFields() {
        RegisterRequest requestReg = userTestUtil.generateRandomUser().toBuilder()
                .username("trim_user")
                .password("A1b!Trim")
                .build();
        String userId = null;
        try {
            Response responseReg = apiHelper.post(AUTH_REGISTER.getPath(), requestReg);
            ApiResponseAssert.assertRegisterSuccess(responseReg, requestReg);
            userId = responseReg.jsonPath().getString(DATA_ID.getPath());
            AuthRequest request = AuthRequest.builder()
                    .username("  trim_user  ")
                    .password("  A1b!Trim  ")
                    .build();
            Response response = apiHelper.post(AUTH_LOGIN.getPath(), request);
            ApiResponseAssert.assertAuthSuccess(response, Integer.parseInt(userId), USER.getEnName());
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    // ====== Негативные сценарии ======

    @Test
    @DisplayName("Login: username правильный, password неверный")
    void loginWithWrongPassword() {
        RegisterRequest requestReg = userTestUtil.generateRandomUser().toBuilder()
                .username(TestDataUtil.randomUsername())
                .password(TestDataUtil.validPassword())
                .build();
        String userId = null;
        try {
            Response responseReg = apiHelper.post(AUTH_REGISTER.getPath(), requestReg);
            ApiResponseAssert.assertRegisterSuccess(responseReg, requestReg);
            userId = responseReg.jsonPath().getString(DATA_ID.getPath());
            AuthRequest request = AuthRequest.builder()
                    .username(requestReg.getUsername())
                    .password(requestReg.getPassword() + "Wrong")
                    .build();
            Response response = apiHelper.post(AUTH_LOGIN.getPath(), request);
            ApiResponseAssert.assertInvalidCredentials(response, INVALID_CREDENTIALS.getMsg());
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test @DisplayName("Login: username неверный, password правильный")
    void loginWithWrongUsername() {
        RegisterRequest requestReg = userTestUtil.generateRandomUser().toBuilder()
                .username(TestDataUtil.randomUsername())
                .password(TestDataUtil.validPassword())
                .build();
        String userId = null;
        try {
            Response responseReg = apiHelper.post(AUTH_REGISTER.getPath(), requestReg);
            ApiResponseAssert.assertRegisterSuccess(responseReg, requestReg);
            userId = responseReg.jsonPath().getString(DATA_ID.getPath());
            AuthRequest request = AuthRequest.builder()
                    .username(requestReg.getUsername() + "Wrong")
                    .password(requestReg.getPassword())
                    .build();
            Response response = apiHelper.post(AUTH_LOGIN.getPath(), request);
            ApiResponseAssert.assertUserNotFound(response, USER_NOT_FOUND.getMsg());
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Login: попытка входа после удаления пользователя")
    void loginAfterUserDeleted() {
        RegisterRequest requestReg = userTestUtil.generateRandomUser().toBuilder()
                .username(TestDataUtil.randomUsername())
                .password(TestDataUtil.validPassword())
                .build();
        String userId = null;
        try {
            Response responseReg = apiHelper.post(AUTH_REGISTER.getPath(), requestReg);
            ApiResponseAssert.assertRegisterSuccess(responseReg, requestReg);
            userId = responseReg.jsonPath().getString(DATA_ID.getPath());
            userTestUtil.deleteUser(userId);
            DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            AuthRequest request = AuthRequest.builder()
                    .username(requestReg.getUsername())
                    .password(requestReg.getPassword())
                    .build();
            Response response = apiHelper.post(AUTH_LOGIN.getPath(), request);
            ApiResponseAssert.assertUserNotFound(response, USER_NOT_FOUND.getMsg());
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    // ====== Структура и формат запроса ======

    @Test
    @DisplayName("Login: лишние поля в body (ignored)")
    void loginWithExtraFields() {
        RegisterRequest requestReg = userTestUtil.generateRandomUser().toBuilder()
                .username(TestDataUtil.randomUsername())
                .password(TestDataUtil.validPassword())
                .build();
        String userId = null;
        try {
            Response responseReg = apiHelper.post(AUTH_REGISTER.getPath(), requestReg);
            ApiResponseAssert.assertRegisterSuccess(responseReg, requestReg);
            userId = responseReg.jsonPath().getString(DATA_ID.getPath());
            Map<String, Object> request = new HashMap<>();
            request.put("username", requestReg.getUsername());
            request.put("password", requestReg.getPassword());
            request.put("extraField", "shouldBeIgnored");
            Response response = apiHelper.post(AUTH_LOGIN.getPath(), request);
            ApiResponseAssert.assertAuthSuccess(response, Integer.parseInt(userId), USER.getEnName());
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Login: пустой body {}")
    void loginWithEmptyBodyObject() {
        Response response = apiHelper.post(AUTH_LOGIN.getPath(), "{}");
        ApiResponseAssert.assertBadRequest(response, USERNAME_EMPTY.getMsg());
    }

    @ParameterizedTest(name = "Login: body = {0} (invalid json format)")
    @MethodSource("invalidBodies")
    void loginWithInvalidJsonBody(String body) {
        Response response = apiHelper.post(AUTH_LOGIN.getPath(), body);
        ApiResponseAssert.assertBadRequest(response, INVALID_OR_EMPTY_JSON.getMsg());
    }

    // ====== Заголовки и Content-Type ======

    @Test @DisplayName("Login: Content-Type отсутствует")
    void loginWithoutContentType() {
        String body = "{\"username\":\"" + TestDataUtil.randomUsername() + "\",\"password\":\"" + TestDataUtil.validPassword() + "\"}";
        Response response = given()
                .body(body)
                .when()
                .post(AUTH_LOGIN.getPath());
        ApiResponseAssert.assertUnsupportedMediaType(response);
    }

    @Test
    @DisplayName("Login: Content-Type = text/plain")
    void loginWithContentTypeTextPlain() {
        String body = "{\"username\":\"" + TestDataUtil.randomUsername() + "\",\"password\":\"" + TestDataUtil.validPassword() + "\"}";
        Response response = given()
                .contentType("text/plain")
                .body(body)
                .when()
                .post(AUTH_LOGIN.getPath());
        ApiResponseAssert.assertUnsupportedMediaType(response);
    }

    @Test
    @DisplayName("Login: Content-Type = application/javascript")
    void loginWithContentTypeJavascript() {
        String body = "{\"username\":\"" + TestDataUtil.randomUsername() + "\",\"password\":\"" + TestDataUtil.validPassword() + "\"}";
        Response response = given()
                .contentType("application/javascript")
                .body(body)
                .when()
                .post(AUTH_LOGIN.getPath());
        ApiResponseAssert.assertUnsupportedMediaType(response);
    }

    @Test
    @DisplayName("Login: Content-Type = application/xml")
    void loginWithContentTypeXml() {
        String xmlBody = "<auth><username>user</username><password>A1b!1234</password></auth>";
        Response response = given()
                .contentType("application/xml")
                .body(xmlBody)
                .when()
                .post(AUTH_LOGIN.getPath());
        ApiResponseAssert.assertUnsupportedMediaType(response);
    }

    @Test
    @DisplayName("Login: Content-Type = text/html")
    void loginWithContentTypeHtml() {
        String htmlBody = "<html><body>Test</body></html>";
        Response response = given()
                .contentType("text/html")
                .body(htmlBody)
                .when()
                .post(AUTH_LOGIN.getPath());
        ApiResponseAssert.assertUnsupportedMediaType(response);
    }

    @Test
    @DisplayName("Login: Content-Type = multipart/form-data")
    void loginWithMultipartFormData() {
        Response response = given()
                .contentType("multipart/form-data")
                .multiPart("username", TestDataUtil.randomUsername())
                .multiPart("password", TestDataUtil.validPassword())
                .when()
                .post(AUTH_LOGIN.getPath());
        ApiResponseAssert.assertUnsupportedMediaType(response);
    }

    @Test
    @DisplayName("Login: Content-Type = application/x-www-form-urlencoded")
    void loginWithFormUrlEncoded() {
        Response response = given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("username", TestDataUtil.randomUsername())
                .formParam("password", TestDataUtil.validPassword())
                .when()
                .post(AUTH_LOGIN.getPath());
        ApiResponseAssert.assertUnsupportedMediaType(response);
    }

    @Test @DisplayName("Login: Custom headers — пользователь успешно авторизован")
    void loginWithCustomHeader() {
        RegisterRequest requestReg = userTestUtil.generateRandomUser().toBuilder()
                .username(TestDataUtil.randomUsername())
                .password(TestDataUtil.validPassword())
                .build();
        String userId = null;
        try {
            Response responseReg = apiHelper.post(AUTH_REGISTER.getPath(), requestReg);
            ApiResponseAssert.assertRegisterSuccess(responseReg, requestReg);
            userId = responseReg.jsonPath().getString(DATA_ID.getPath());
            String body = "{\"username\":\"" + requestReg.getUsername() + "\",\"password\":\"" + requestReg.getPassword() + "\"}";
            Response response = given()
                    .header("X-Custom-Header", "test")
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when()
                    .post(AUTH_LOGIN.getPath());
            ApiResponseAssert.assertAuthSuccess(response, Integer.parseInt(userId), USER.getEnName());
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    // ====== HTTP-метод и endpoint ======

    @Test
    @DisplayName("Login: GET вместо POST")
    void loginWithGetMethod() {
        String get = "GET";
        Response response = given().when().get(AUTH_LOGIN.getPath());
        ApiResponseAssert.assertMethodNotAllowed(response, get);
    }

    @Test
    @DisplayName("Login: PUT вместо POST")
    void loginWithPutMethod() {
        String put = "PUT";
        Response response = given()
                .contentType(ContentType.JSON)
                .body("{\"username\":\"user\",\"password\":\"A1b!1234\"}")
                .when()
                .put(AUTH_LOGIN.getPath());
        ApiResponseAssert.assertMethodNotAllowed(response, put);
    }

    @Test
    @DisplayName("Login: DELETE вместо POST")
    void loginWithDeleteMethod() {
        String delete = "DELETE";
        Response response = given()
                .contentType(ContentType.JSON)
                .body("{\"username\":\"user\",\"password\":\"A1b!1234\"}")
                .when()
                .delete(AUTH_LOGIN.getPath());
        ApiResponseAssert.assertMethodNotAllowed(response, delete);
    }

    @Test
    @DisplayName("Login: несуществующий endpoint")
    void loginNotExistingEndpoint() {
        Response response = given().post(AUTH_NOT_EXIST.getPath());
        ApiResponseAssert.assertError(
                response,
                ApiError.NOT_FOUND.getStatus(),
                ApiError.NOT_FOUND.getCode(),
                ENDPOINT_NOT_FOUND.getMsg());
    }

    // ====== HTTP-ответ (meta) ======

    @Test @DisplayName("Login: Ответ не содержит Set-Cookie")
    void loginNoSetCookieInResponse() {
        RegisterRequest requestReg = userTestUtil.generateRandomUser().toBuilder()
                .username(TestDataUtil.randomUsername())
                .password(TestDataUtil.validPassword())
                .build();
        String userId = null;
        try {
            Response responseReg = apiHelper.post(AUTH_REGISTER.getPath(), requestReg);
            ApiResponseAssert.assertRegisterSuccess(responseReg, requestReg);
            userId = responseReg.jsonPath().getString(DATA_ID.getPath());
            String body = "{\"username\":\"" + requestReg.getUsername() + "\",\"password\":\"" + requestReg.getPassword() + "\"}";
            given()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when()
                    .post(AUTH_LOGIN.getPath())
                    .then()
                    .statusCode(OK.getStatus())
                    .header(ApiHeader.SET_COOKIE.getTitle(), nullValue());
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test @DisplayName("Login: Ответ содержит Cache-Control и Pragma")
    void loginHasCacheHeaders() {
        RegisterRequest requestReg = userTestUtil.generateRandomUser().toBuilder()
                .username(TestDataUtil.randomUsername())
                .password(TestDataUtil.validPassword())
                .build();
        String userId = null;
        try {
            Response responseReg = apiHelper.post(AUTH_REGISTER.getPath(), requestReg);
            ApiResponseAssert.assertRegisterSuccess(responseReg, requestReg);
            userId = responseReg.jsonPath().getString(DATA_ID.getPath());
            String body = "{\"username\":\"" + requestReg.getUsername() + "\",\"password\":\"" + requestReg.getPassword() + "\"}";
            given()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when()
                    .post(AUTH_LOGIN.getPath())
                    .then()
                    .statusCode(OK.getStatus())
                    .header(ApiHeader.CACHE_CONTROL.getTitle(), notNullValue())
                    .header(ApiHeader.PRAGMA.getTitle(), notNullValue());
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Login: Время ответа <2 сек (200 OK)")
    void loginResponseTime200() {
        RegisterRequest requestReg = userTestUtil.generateRandomUser().toBuilder()
                .username(TestDataUtil.randomUsername())
                .password(TestDataUtil.validPassword())
                .build();
        String userId = null;
        try {
            Response responseReg = apiHelper.post(AUTH_REGISTER.getPath(), requestReg);
            ApiResponseAssert.assertRegisterSuccess(responseReg, requestReg);
            userId = responseReg.jsonPath().getString(DATA_ID.getPath());
            String body = "{\"username\":\"" + requestReg.getUsername() + "\",\"password\":\"" + requestReg.getPassword() + "\"}";
            given()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when()
                    .post(AUTH_LOGIN.getPath())
                    .then()
                    .statusCode(OK.getStatus())
                    .time(lessThan(2000L));
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Login: Время ответа <2 сек (400 Bad Request)")
    void loginResponseTime400() {
        AuthRequest request = AuthRequest.builder()
                .username("")
                .password("")
                .build();
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(AUTH_LOGIN.getPath())
                .then()
                .statusCode(ApiError.BAD_REQUEST.getStatus())
                .time(lessThan(2000L));
    }

    @Test
    @DisplayName("Login: Время ответа <2 сек (415 Unsupported Media Type)")
    void loginResponseTime415() {
        String body = "{\"username\":\"user\",\"password\":\"A1b!1234\"}";
        given()
                .contentType("text/plain")
                .body(body)
                .when()
                .post(AUTH_LOGIN.getPath())
                .then()
                .statusCode(UNSUPPORTED_MEDIA_TYPE.getStatus())
                .time(lessThan(2000L));
    }

    @Test
    @DisplayName("Login: Время ответа <2 сек (404 Not Found)")
    void loginResponseTime404() {
        given()
                .when()
                .post("/auth/not-exist")
                .then()
                .statusCode(NOT_FOUND.getStatus())
                .time(lessThan(2000L));
    }

    @Test
    @DisplayName("Login: Время ответа <2 сек (405 Method not allowed)")
    void loginResponseTime405() {
        given()
                .when()
                .get(AUTH_LOGIN.getPath())
                .then()
                .statusCode(METHOD_NOT_ALLOWED.getStatus())
                .time(lessThan(2000L));
    }

    @Test
    @DisplayName("Login: Flood/Rate limit (5+ логинов подряд)")
    void loginFloodLimit() {
        RegisterRequest requestReg = userTestUtil.generateRandomUser().toBuilder()
                .username(TestDataUtil.randomUsername())
                .password(TestDataUtil.validPassword())
                .build();
        String userId = null;
        try {
            Response responseReg = apiHelper.post(AUTH_REGISTER.getPath(), requestReg);
            ApiResponseAssert.assertRegisterSuccess(responseReg, requestReg);
            userId = responseReg.jsonPath().getString(DATA_ID.getPath());
            for (int i = 0; i < 7; i++) {
                AuthRequest request = AuthRequest.builder()
                        .username(requestReg.getUsername())
                        .password(requestReg.getPassword())
                        .build();
                Response response = apiHelper.post(AUTH_LOGIN.getPath(), request);
                ApiResponseAssert.assertAuthSuccess(response, Integer.parseInt(userId), USER.getEnName());
            }
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Login: CORS preflight OPTIONS возвращает 200 и CORS-заголовки")
    void loginCorsOptionsRequest() {
        String origin = "http://localhost:3000";
        String method = "POST";
        given()
                .header("Origin", origin)
                .header("Access-Control-Request-Method", method)
                .when()
                .options(AUTH_LOGIN.getPath())
                .then()
                .statusCode(ApiError.OK.getStatus())
                .header(ApiHeader.ACCESS_CONTROL_ALLOW_ORIGIN.getTitle(), equalTo(origin))
                .header(ApiHeader.ACCESS_CONTROL_ALLOW_METHODS.getTitle(), equalTo(method))
                .header(ApiHeader.ACCESS_CONTROL_ALLOW_CREDENTIALS.getTitle(), "true");
    }
}

/*
 * ЧЕКЛИСТ авторизации /auth/login (positive + negative)
 *
 * ====== Валидация username ======
 * [ ] Login: username = пусто
 * [ ] Login: username = пробел
 * [ ] Login: username = null
 * [ ] Login: username = int вместо строки
 * [ ] Login: username = 1 символ
 * [ ] Login: username = 2 символа (валид)
 * [ ] Login: username = 32 символа (валид)
 * [ ] Login: username = 33 символа (невалид)
 * [ ] Login: username с emoji/юникодом
 * [ ] Login: username с XSS-инъекцией
 * [ ] Login: username с SQL-инъекцией
 *
 * ====== Валидация password ======
 * [ ] Login: password = пусто
 * [ ] Login: password = пробел
 * [ ] Login: password = null
 * [ ] Login: password = int вместо строки
 * [ ] Login: password = 5 символов (невалид)
 * [ ] Login: password = 6 символов (валид)
 * [ ] Login: password = 64 символа (валид)
 * [ ] Login: password = 65 символов (невалид)
 * [ ] Login: password с пробелом посередине
 * [ ] Login: password с emoji/юникодом
 * [ ] Login: password с XSS-инъекцией
 * [ ] Login: password с SQL-инъекцией
 *
 * ====== Позитивные сценарии ======
 * [ ] Login: Happy path USER
 * [ ] Login: Happy path ADMIN
 * [ ] Login: username и password c пробелами по краям
 *
 * ====== Негативные сценарии ======
 * [ ] Login: username правильный, password неверный
 * [ ] Login: username неверный, password правильный
 * [ ] Login: попытка входа после удаления пользователя
 *
 * ====== Структура и формат запроса ======
 * [ ] Login: лишние поля в body (ignored)
 * [ ] Login: пустой body ""
 * [ ] Login: пустой body {}
 * [ ] Login: нет body/null вместо объекта
 * [ ] Login: массив вместо объекта
 * [ ] Login: число вместо объекта
 * [ ] Login: строка вместо объекта
 * [ ] Login: невалидный JSON
 *
 * ====== Заголовки и Content-Type ======
 * [ ] Login: Content-Type отсутствует
 * [ ] Login: Content-Type = text/plain
 * [ ] Login: Content-Type = application/javascript
 * [ ] Login: Content-Type = application/xml
 * [ ] Login: Content-Type = text/html
 * [ ] Login: Content-Type = multipart/form-data
 * [ ] Login: Content-Type = application/x-www-form-urlencoded
 * [ ] Login: Custom headers — пользователь успешно авторизован
 *
 * ====== HTTP-метод и endpoint ======
 * [ ] Login: GET вместо POST
 * [ ] Login: PUT вместо POST
 * [ ] Login: DELETE вместо POST
 * [ ] Login: несуществующий endpoint
 *
 * ====== HTTP-ответ (meta) ======
 * [ ] Login: Ответ не содержит Set-Cookie
 * [ ] Login: Ответ содержит Cache-Control и Pragma
 * [ ] Login: Время ответа <2 сек (200 OK)
 * [ ] Login: Время ответа <2 сек (400 Bad Request)
 * [ ] Login: Время ответа <2 сек (415 Unsupported Media Type)
 * [ ] Login: Время ответа <2 сек (404 Not Found)
 * [ ] Login: Время ответа <2 сек (405 Method not allowed)
 * [ ] Login: Flood/Rate limit (5+ логинов подряд)
 * [ ] Login: CORS preflight OPTIONS возвращает 200 и CORS-заголовки
 */
