package bookShop.apiTests.tests.auth;

import bookShop.apiTests.common.BaseIntegrationTest;
import bookShop.apiTests.model.RegisterRequest;
import bookShop.apiTests.common.ApiResponseAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import io.restassured.response.Response;
import bookShop.apiTests.common.TestDataUtil;
import java.util.HashMap;
import java.util.Map;
import bookShop.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import bookShop.apiTests.common.DbResponseAssert;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.List;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import bookShop.apiTests.enums.ApiError;
import bookShop.apiTests.enums.ErrorMessage;
import bookShop.apiTests.enums.ApiError;
import bookShop.apiTests.enums.ApiHeader;
import bookShop.apiTests.enums.ApiPath;
import bookShop.apiTests.enums.ErrorMessage;
import bookShop.apiTests.enums.JsonPathKey;

import static bookShop.apiTests.enums.ApiError.FORBIDDEN;
import static bookShop.apiTests.enums.ApiError.METHOD_NOT_ALLOWED;
import static bookShop.apiTests.enums.ApiError.NOT_FOUND;
import static bookShop.apiTests.enums.ApiError.OK;
import static bookShop.apiTests.enums.ApiError.UNSUPPORTED_MEDIA_TYPE;
import static bookShop.apiTests.enums.ApiError.USER_ALREADY_EXISTS;
import static bookShop.apiTests.enums.ApiPath.AUTH_REGISTER;
import static bookShop.apiTests.enums.ApiPath.AUTH_REGISTER_NOT_EXIST;
import static bookShop.apiTests.enums.ErrorMessage.ADMIN_LIMIT;
import static bookShop.apiTests.enums.ErrorMessage.EMAIL_EMPTY;
import static bookShop.apiTests.enums.ErrorMessage.EMAIL_FORMAT;
import static bookShop.apiTests.enums.ErrorMessage.EMAIL_LENGTH;
import static bookShop.apiTests.enums.ErrorMessage.EMPTY_PASSWORD;
import static bookShop.apiTests.enums.ErrorMessage.EMPTY_USERNAME;
import static bookShop.apiTests.enums.ErrorMessage.ENDPOINT_NOT_FOUND;
import static bookShop.apiTests.enums.ErrorMessage.FORBIDDEN_CHARS;
import static bookShop.apiTests.enums.ErrorMessage.INVALID_OR_EMPTY_JSON;
import static bookShop.apiTests.enums.ErrorMessage.PHONE_EMPTY;
import static bookShop.apiTests.enums.ErrorMessage.PHONE_INVALID;
import static bookShop.apiTests.enums.ErrorMessage.USERNAME_LENGTH;
import static bookShop.apiTests.enums.JsonPathKey.DATA_ID;
import static bookShop.model.Role.USER;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static bookShop.model.Role.*;

@DisplayName("Auth /register: all fields headers positive/negative")
public class AuthRegisterValidationIT extends BaseIntegrationTest {

    @Autowired
    private AppUserRepository appUserRepository;

    private final List<String> createdUserIds = new ArrayList<>();

    private static Stream<Arguments> invalidPasswords() {
        return Stream.of(
                Arguments.of("testpass1!", "без заглавной буквы"),
                Arguments.of("TESTPASS1!", "без маленькой буквы"),
                Arguments.of("TEStPaSs!", "без цифры"),
                Arguments.of("Testpass1", "без спецсимвола"),
                Arguments.of("Testpass1!ф", "с русской буквой"),
                Arguments.of("Test pass1!", "с пробелом посередине")
        );
    }

    private static Stream<Arguments> invalidRoles() {
        return Stream.of(
                Arguments.of("", "пустая строка"),
                Arguments.of(" ", "пробел"),
                Arguments.of(null, "null значение")
        );
    }

    static Stream<Arguments> invalidPhoneFormats() {
        return Stream.of(
                Arguments.of("1234567890", "phone = 10 цифр"),
                Arguments.of("123456789012", "phone = 12 цифр"),
                Arguments.of("+7(999)000-11-22", "phone формат со скобками"),
                Arguments.of("abcdefghijk", "phone не int (буквы)")
        );
    }

    private static Stream<Arguments> invalidEmailCases() {
        return Stream.of(
                Arguments.of("@mail.ru", "пусто до @"),
                Arguments.of("test~@mail.ru", "~ до @"),
                Arguments.of("&test*@mail.ru", "& и * до @"),
                Arguments.of("test@.ru", "пусто после @"),
                Arguments.of("test @mail.ru", "пробел до @"),
                Arguments.of("test@ mail.ru", "пробел после @"),
                Arguments.of("test@+mail.ru", "+ после @"),
                Arguments.of("test@mail%.ru", "% после @"),
                Arguments.of("test@mail .ru", "пробел перед точкой"),
                Arguments.of("test@mailru", "без точки"),
                Arguments.of("test@mail. ru", "пробел после точки"),
                Arguments.of("testmail.ru", "без @"),
                Arguments.of("test@@mail.ru", "две @"),
                Arguments.of("test@mail.r", "некорректная доменная зона (r вместо ru)"),
                Arguments.of("русские@буквы.ру", "кириллица")
        );
    }

    static Stream<Arguments> invalidUsernames() {
        return Stream.of(
                Arguments.of("", "пусто"),
                Arguments.of(" ", "пробел"),
                Arguments.of(null, "null")
        );
    }

    static Stream<Arguments> forbiddenUsernameCases() {
        return Stream.of(
                Arguments.of("emojii😀", "emoji/unicode"),
                Arguments.of("<script>alert('xss')</script>", "XSS-инъекция"),
                Arguments.of("user'; DROP TABLE users;--", "SQL-инъекция")
        );
    }

    static Stream<Arguments> emptyOrNullPasswordCases() {
        return Stream.of(
                Arguments.of("", "пусто"),
                Arguments.of(" ", "пробел"),
                Arguments.of(null, "null")
        );
    }

    static Stream<Arguments> invalidPasswordSymbolCases() {
        return Stream.of(
                Arguments.of("emojii😀", "emoji/юникод"),
                Arguments.of("<script>alert('xss')</script>", "XSS-инъекция"),
                Arguments.of("user'; DROP TABLE users;--", "SQL-инъекция")
        );
    }

    static Stream<Arguments> invalidRoleSymbolCases() {
        return Stream.of(
                Arguments.of("BOSS", "другое значение"),
                Arguments.of("emojii😀", "emoji/юникод"),
                Arguments.of("<script>alert('xss')</script>", "XSS-инъекция"),
                Arguments.of("user'; DROP TABLE users;--", "SQL-инъекция")
        );
    }

    static Stream<Arguments> invalidPhoneEmptyCases() {
        return Stream.of(
                Arguments.of(null, PHONE_EMPTY.getMsg()),
                Arguments.of("", PHONE_EMPTY.getMsg()),
                Arguments.of(" ", PHONE_EMPTY.getMsg())
        );
    }

    static Stream<Arguments> invalidPhoneSpecialCases() {
        return Stream.of(
                Arguments.of("emojii😀", "emoji/юникод"),
                Arguments.of("<script>alert('xss')</script>", "XSS-инъекция"),
                Arguments.of("user'; DROP TABLE users;--", "SQL-инъекция")
        );
    }

    static Stream<Arguments> invalidEmailEmptyCases() {
        return Stream.of(
                Arguments.of(null, "null", true),
                Arguments.of("", "пусто", false),
                Arguments.of(" ", "пробел", false)
        );
    }

    static Stream<Arguments> invalidEmailSpecialCases() {
        return Stream.of(
                Arguments.of("emojii😀@mail.ru", "emoji/юникод"),
                Arguments.of("<script>alert('xss')</script>", "XSS-инъекция"),
                Arguments.of("user'; DROP TABLE users;--", "SQL-инъекция")
        );
    }

    static Stream<Arguments> invalidJsonRequests() {
        return Stream.of(
                Arguments.of("[{ \"username\": \"user\" }]", "массив вместо объекта"),
                Arguments.of("12345", "число вместо объекта"),
                Arguments.of("\"justastring\"", "строка вместо объекта"),
                Arguments.of("null", "null вместо объекта"),
                Arguments.of("{username:123}", "нет кавычек вокруг ключа"),
                Arguments.of("{ \"username\": \"user\", ", "нет закрывающей скобки")
        );
    }

    // ====== Валидация username ======

    @ParameterizedTest(name = "[{index}] username=\"{0}\" — {1}")
    @MethodSource("invalidUsernames")
    @DisplayName("Register: username невалиден (пусто/пробел/null)")
    void registerWithInvalidUsername(String username, String description) {
        RegisterRequest request = userTestUtil.generateRandomUser().toBuilder()
                .username(username)
                .build();
        Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
        if (username == null) {
            ApiResponseAssert.assertError(
                    response,
                    ApiError.BAD_REQUEST.getStatus(),
                    ApiError.BAD_REQUEST.getCode(),
                    EMPTY_USERNAME.getMsg()
            );
        } else {
            ApiResponseAssert.assertErrorPartly(
                    response,
                    ApiError.BAD_REQUEST.getStatus(),
                    ApiError.BAD_REQUEST.getCode(),
                    EMPTY_USERNAME.getMsg()
            );
        }
        DbResponseAssert.assertUserNotExistsInDb(appUserRepository, request.getUsername());
    }

    @Test
    @DisplayName("Register: username = int вместо строки")
    void registerWithIntUsername() {
        String password = TestDataUtil.validPassword();
        String email = TestDataUtil.randomEmail();
        String phone = TestDataUtil.randomPhone();
        int usernameAsInt = 1012345;
        Map<String, Object> request = new HashMap<>();
        request.put("username", usernameAsInt);
        request.put("password", password);
        request.put("role", USER);
        request.put("phone", phone);
        request.put("email", email);
        String userId = null;
        try {
            Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
            ApiResponseAssert.assertRegisterSuccess(
                    response,
                    email,
                    String.valueOf(usernameAsInt),
                    USER.name(),
                    phone
            );
            userId = response.jsonPath().getString(DATA_ID.getPath());
            DbResponseAssert.assertUserCorrectInDb(
                    appUserRepository,
                    String.valueOf(usernameAsInt),
                    email,
                    phone,
                    USER);
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Register: username = int с лидирующим нулём")
    void registerWithIntUsernameLeadingZero() {
        String password = TestDataUtil.validPassword();
        String email = TestDataUtil.randomEmail();
        String phone = TestDataUtil.randomPhone();
        String badRequestJson = "{"
                + "\"username\": 012345,"
                + "\"password\": \"" + password + "\","
                + "\"role\": \"USER\","
                + "\"phone\": \"" + phone + "\","
                + "\"email\": \"" + email + "\""
                + "}";
        long countBefore = appUserRepository.count();
        Response response = apiHelper.post(AUTH_REGISTER.getPath(), badRequestJson);
        ApiResponseAssert.assertError(
                response,
                ApiError.BAD_REQUEST.getStatus(),
                ApiError.BAD_REQUEST.getCode(),
                INVALID_OR_EMPTY_JSON.getMsg()
        );
        long countAfter = appUserRepository.count();
        assertEquals(countBefore, countAfter, "В БД не должно появиться новых пользователей");
    }

    @Test
    @DisplayName("Register: username = 1 символ")
    void registerWithUsername1Symbol() {
        RegisterRequest request = userTestUtil.generateRandomUser().toBuilder()
                .username("a")
                .build();
        Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
        ApiResponseAssert.assertError(
                response,
                ApiError.BAD_REQUEST.getStatus(),
                ApiError.BAD_REQUEST.getCode(),
                USERNAME_LENGTH.getMsg()
        );
        DbResponseAssert.assertUserNotExistsInDb(appUserRepository, request.getUsername());
    }

    @Test
    @DisplayName("Register: username = 2 символа")
    void registerWithUsername2Symbols() {
        RegisterRequest request = userTestUtil.generateRandomUser().toBuilder()
                .username("av")
                .build();
        String userId = null;
        try {
            Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
            ApiResponseAssert.assertRegisterSuccess(
                    response,
                    request.getEmail(),
                    request.getUsername(),
                    request.getRole(),
                    request.getPhone()
            );
            userId = response.jsonPath().getString(DATA_ID.getPath());
            DbResponseAssert.assertUserCorrectInDb(
                    appUserRepository,
                    request.getUsername(),
                    request.getEmail(),
                    request.getPhone(),
                    USER);
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Register: username = 32 символа")
    void registerWithUsername32Symbols() {
        RegisterRequest request = userTestUtil.generateRandomUser().toBuilder()
                .username("b".repeat(32))
                .build();
        String userId = null;
        try {
            Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
            ApiResponseAssert.assertRegisterSuccess(
                    response,
                    request.getEmail(),
                    request.getUsername(),
                    request.getRole(),
                    request.getPhone()
            );
            userId = response.jsonPath().getString(DATA_ID.getPath());
            DbResponseAssert.assertUserCorrectInDb(
                    appUserRepository,
                    request.getUsername(),
                    request.getEmail(),
                    request.getPhone(),
                    USER);
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Register: username = 33 символа")
    void registerWithUsername33Symbols() {
        RegisterRequest request = userTestUtil.generateRandomUser().toBuilder()
                .username("b".repeat(33))
                .build();
        Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
        ApiResponseAssert.assertError(
                response,
                ApiError.BAD_REQUEST.getStatus(),
                ApiError.BAD_REQUEST.getCode(),
                USERNAME_LENGTH.getMsg()
        );
        DbResponseAssert.assertUserNotExistsInDb(appUserRepository, request.getUsername());
    }

    @ParameterizedTest(name = "[{index}] username=\"{0}\" — {1}")
    @MethodSource("forbiddenUsernameCases")
    @DisplayName("Register: username содержит запрещённые символы (emoji/XSS/SQL)")
    void registerWithForbiddenUsernameSymbols(String username, String description) {
        RegisterRequest request = userTestUtil.generateRandomUser().toBuilder()
                .username(username)
                .build();
        long countBefore = appUserRepository.count();
        Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
        ApiResponseAssert.assertErrorPartly(
                response,
                ApiError.BAD_REQUEST.getStatus(),
                ApiError.BAD_REQUEST.getCode(),
                FORBIDDEN_CHARS.getMsg()
        );
        long countAfter = appUserRepository.count();
        assertEquals(countBefore, countAfter, "В БД не должно появиться новых пользователей (" + description + ")");
    }

    @Test
    @DisplayName("Register: регистрация с разным регистром username")
    void registerSameUsernameDifferentCase() {
        RegisterRequest first = userTestUtil.generateRandomUser().toBuilder()
                .username("USER")
                .build();
        String userIdFirst = null;
        String userIdSecond = null;
        try {
            Response firstResponse = apiHelper.post(AUTH_REGISTER.getPath(), first);
            ApiResponseAssert.assertRegisterSuccess(
                    firstResponse,
                    first.getEmail(),
                    first.getUsername(),
                    first.getRole(),
                    first.getPhone()
            );
            userIdFirst = firstResponse.jsonPath().getString(DATA_ID.getPath());
            DbResponseAssert.assertUserCorrectInDb(
                    appUserRepository,
                    first.getUsername(),
                    first.getEmail(),
                    first.getPhone(),
                    USER);
            RegisterRequest second = userTestUtil.generateRandomUser().toBuilder()
                    .username("user")
                    .build();
            Response secondResponse = apiHelper.post(AUTH_REGISTER.getPath(), second);
            ApiResponseAssert.assertRegisterSuccess(
                    secondResponse,
                    second.getEmail(),
                    second.getUsername(),
                    second.getRole(),
                    second.getPhone()
            );
            userIdSecond = secondResponse.jsonPath().getString(DATA_ID.getPath());
            DbResponseAssert.assertUserCorrectInDb(
                    appUserRepository,
                    second.getUsername(),
                    second.getEmail(),
                    second.getPhone(),
                    USER);
        } finally {
            if (userIdFirst != null || userIdSecond != null) {
                userTestUtil.deleteUser(userIdFirst);
                userTestUtil.deleteUser(userIdSecond);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userIdFirst);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userIdSecond);
            }
        }
    }

    // ====== Валидация password ======

    @ParameterizedTest(name = "[{index}] password=\"{0}\" — {1}")
    @MethodSource("emptyOrNullPasswordCases")
    @DisplayName("Register: password = пусто/пробел/null")
    void registerWithEmptyOrNullPassword(String password, String description) {
        RegisterRequest request = userTestUtil.generateRandomUser().toBuilder()
                .password(password)
                .build();
        Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
        ApiResponseAssert.assertErrorPartly(
                response,
                ApiError.BAD_REQUEST.getStatus(),
                ApiError.BAD_REQUEST.getCode(),
                EMPTY_PASSWORD.getMsg());
        DbResponseAssert.assertUserNotExistsInDb(appUserRepository, request.getUsername());
    }

    @Test
    @DisplayName("Register: password = int вместо строки")
    void registerWithIntPassword() {
        String username = TestDataUtil.randomUsername();
        String email = TestDataUtil.randomEmail();
        String phone = TestDataUtil.randomPhone();
        int passwordAsInt = 1012345;
        Map<String, Object> request = new HashMap<>();
        request.put("username", username);
        request.put("password", passwordAsInt);
        request.put("role", "USER");
        request.put("phone", phone);
        request.put("email", email);
        Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
        ApiResponseAssert.assertErrorPartly(
                response,
                ApiError.BAD_REQUEST.getStatus(),
                ApiError.BAD_REQUEST.getCode(),
                EMPTY_PASSWORD.getMsg());
        DbResponseAssert.assertUserNotExistsInDb(appUserRepository, username);
    }

    @Test
    @DisplayName("Register: password = int с лидирующим нулём")
    void registerWithIntPasswordLeadingZero() {
        String username = TestDataUtil.randomUsername();
        String email = TestDataUtil.randomEmail();
        String phone = TestDataUtil.randomPhone();
        String badRequestJson = "{"
                + "\"username\": \"" + username + "\","
                + "\"password\": 0123456,"
                + "\"role\": \"USER\","
                + "\"phone\": \"" + phone + "\","
                + "\"email\": \"" + email + "\""
                + "}";
        Response response = apiHelper.post(AUTH_REGISTER.getPath(), badRequestJson);
        ApiResponseAssert.assertError(
                response,
                ApiError.BAD_REQUEST.getStatus(),
                ApiError.BAD_REQUEST.getCode(),
                INVALID_OR_EMPTY_JSON.getMsg()
        );
        DbResponseAssert.assertUserNotExistsInDb(appUserRepository, username);
    }

    @Test
    @DisplayName("Register: password = 5 символов (невалид)")
    void registerWithPassword5Symbols() {
        RegisterRequest request = userTestUtil.generateRandomUser().toBuilder()
                .password("A1b!2")
                .build();
        Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
        ApiResponseAssert.assertErrorPartly(
                response,
                ApiError.BAD_REQUEST.getStatus(),
                ApiError.BAD_REQUEST.getCode(),
                EMPTY_PASSWORD.getMsg());
        DbResponseAssert.assertUserNotExistsInDb(appUserRepository, request.getUsername());
    }

    @Test
    @DisplayName("Register: password = 6 символов (валид)")
    void registerWithPassword6Symbols() {
        RegisterRequest request = userTestUtil.generateRandomUser().toBuilder()
                .password("A1b!r2")
                .build();
        String userId = null;
        try {
            Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
            ApiResponseAssert.assertRegisterSuccess(
                    response,
                    request.getEmail(),
                    request.getUsername(),
                    request.getRole(),
                    request.getPhone()
            );
            userId = response.jsonPath().getString(DATA_ID.getPath());
            DbResponseAssert.assertUserCorrectInDb(
                    appUserRepository,
                    request.getUsername(),
                    request.getEmail(),
                    request.getPhone(),
                    USER);
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Register: password = 64 символа (валид)")
    void registerWithPassword64Symbols() {
        RegisterRequest request = userTestUtil.generateRandomUser().toBuilder()
                .password("A1b@" + "a".repeat(60))
                .build();
        String userId = null;
        try {
            Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
            ApiResponseAssert.assertRegisterSuccess(
                    response,
                    request.getEmail(),
                    request.getUsername(),
                    request.getRole(),
                    request.getPhone()
            );
            userId = response.jsonPath().getString(DATA_ID.getPath());
            DbResponseAssert.assertUserCorrectInDb(
                    appUserRepository,
                    request.getUsername(),
                    request.getEmail(),
                    request.getPhone(),
                    USER);
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Register: password = 65 символов (невалид)")
    void registerWithPassword65Symbols() {
        RegisterRequest request = userTestUtil.generateRandomUser().toBuilder()
                .password("A1b!" + "a".repeat(61))
                .build();
        Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
        ApiResponseAssert.assertErrorPartly(
                response,
                ApiError.BAD_REQUEST.getStatus(),
                ApiError.BAD_REQUEST.getCode(),
                EMPTY_PASSWORD.getMsg());
        DbResponseAssert.assertUserNotExistsInDb(appUserRepository, request.getUsername());
    }

    @ParameterizedTest(name = "[{index}] password=\"{0}\" — {1}")
    @MethodSource("invalidPasswords")
    @DisplayName("Register: password невалиден (параметризованный)")
    void registerWithInvalidPassword(String password, String caseDescription) {
        RegisterRequest request = userTestUtil.generateRandomUser().toBuilder()
                .password(password)
                .build();
        Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
        ApiResponseAssert.assertErrorPartly(
                response,
                ApiError.BAD_REQUEST.getStatus(),
                ApiError.BAD_REQUEST.getCode(),
                EMPTY_PASSWORD.getMsg());
        assertTrue(response.asString().contains("password"),
                "Ошибка должна относиться к паролю (" + caseDescription + ")");
        DbResponseAssert.assertUserNotExistsInDb(appUserRepository, request.getUsername());
    }

    @ParameterizedTest(name = "[{index}] password=\"{0}\" — {1}")
    @MethodSource("invalidPasswordSymbolCases")
    @DisplayName("Register: password с emoji/юникодом/XSS/SQL-инъекцией")
    void registerWithInvalidPasswordSymbols(String password, String description) {
        RegisterRequest request = userTestUtil.generateRandomUser().toBuilder()
                .password(password)
                .build();
        Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
        ApiResponseAssert.assertErrorPartly(
                response,
                ApiError.BAD_REQUEST.getStatus(),
                ApiError.BAD_REQUEST.getCode(),
                FORBIDDEN_CHARS.getMsg()
        );
        DbResponseAssert.assertUserNotExistsInDb(appUserRepository, request.getUsername());
    }

    // ====== Валидация role ======

    @Test
    @DisplayName("Register: role = буквы разных регистров")
    void registerWithUpAndLowerCasesRole() {
        RegisterRequest request = userTestUtil.generateRandomUser().toBuilder()
                .role("UseR")
                .build();
        String userId = null;
        try {
            Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
            ApiResponseAssert.assertRegisterSuccess(
                    response,
                    request.getEmail(),
                    request.getUsername(),
                    USER.getEnName(),
                    request.getPhone()
            );
            userId = response.jsonPath().getString(DATA_ID.getPath());
            DbResponseAssert.assertUserCorrectInDb(
                    appUserRepository,
                    request.getUsername(),
                    request.getEmail(),
                    request.getPhone(),
                    USER);
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Register: role = маленькими буквами")
    void registerWithLowerCaseRole() {
        RegisterRequest request = userTestUtil.generateRandomUser().toBuilder()
                .role("user")
                .build();
        String userId = null;
        try {
            Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
            ApiResponseAssert.assertRegisterSuccess(
                    response,
                    request.getEmail(),
                    request.getUsername(),
                    USER.getEnName(),
                    request.getPhone()
            );
            userId = response.jsonPath().getString(DATA_ID.getPath());
            DbResponseAssert.assertUserCorrectInDb(
                    appUserRepository,
                    request.getUsername(),
                    request.getEmail(),
                    request.getPhone(),
                    USER);
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @ParameterizedTest(name = "[{index}] role=\"{0}\" — {1}")
    @MethodSource("invalidRoles")
    @DisplayName("Register: невалидная роль (параметризованный)")
    void registerWithInvalidRole(String role, String caseDescription) {
        RegisterRequest request = userTestUtil.generateRandomUser().toBuilder()
                .role(role)
                .build();
        Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
        assertTrue(response.asString().contains("role") || response.asString().contains("role: Роль пользователя обязательна; "),
                "Ошибка должна относиться к роли (" + caseDescription + ")");
        ApiResponseAssert.assertError(
                response,
                ApiError.BAD_REQUEST.getStatus(),
                ApiError.BAD_REQUEST.getCode(),
                "role: Роль пользователя обязательна; "
        );
        DbResponseAssert.assertUserNotExistsInDb(appUserRepository, request.getUsername());
    }

    @Test
    @DisplayName("Register: role = int вместо строки")
    void registerWithIntRole() {
        String username = TestDataUtil.randomUsername();
        String email = TestDataUtil.randomEmail();
        String phone = TestDataUtil.randomPhone();
        String password = TestDataUtil.validPassword();
        int role = 123456;
        Map<String, Object> request = new HashMap<>();
        request.put("username", username);
        request.put("password", password);
        request.put("role", role);
        request.put("phone", phone);
        request.put("email", email);
        Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
        ApiResponseAssert.assertError(
                response,
                ApiError.BAD_REQUEST.getStatus(),
                ApiError.BAD_REQUEST.getCode(),
                INVALID_OR_EMPTY_JSON.getMsg()
        );
        DbResponseAssert.assertUserNotExistsInDb(appUserRepository, username);
    }

    @ParameterizedTest(name = "[{index}] role=\"{0}\" — {1}")
    @MethodSource("invalidRoleSymbolCases")
    @DisplayName("Register: role = другое значение/emoji/юникод/XSS/SQL-инъекция")
    void registerWithInvalidRoleSymbols(String role, String description) {
        RegisterRequest request = userTestUtil.generateRandomUser().toBuilder()
                .role(role)
                .build();
        Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
        ApiResponseAssert.assertError(
                response,
                ApiError.BAD_REQUEST.getStatus(),
                ApiError.BAD_REQUEST.getCode(),
                INVALID_OR_EMPTY_JSON.getMsg()
        );
        DbResponseAssert.assertUserNotExistsInDb(appUserRepository, request.getUsername());
    }

    // ====== Валидация phone ======

    @ParameterizedTest(name = "[{index}] phone=\"{0}\" — {1}")
    @MethodSource("invalidPhoneEmptyCases")
    @DisplayName("Register: phone = null/пусто/пробел")
    void registerWithEmptyOrNullPhone(String phone, String expectedError) {
        RegisterRequest request = userTestUtil.generateRandomUser().toBuilder()
                .phone(phone)
                .build();
        Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
        ApiResponseAssert.assertErrorPartly(
                response,
                ApiError.BAD_REQUEST.getStatus(),
                ApiError.BAD_REQUEST.getCode(),
                expectedError
        );
        DbResponseAssert.assertUserNotExistsInDb(appUserRepository, request.getUsername());
    }

    @ParameterizedTest(name = "[{index}] phone=\"{0}\" — {1}")
    @MethodSource("invalidPhoneFormats")
    @DisplayName("Register: phone невалиден (параметризованный)")
    void registerWithInvalidPhoneFormats(String phone, String caseDescription) {
        RegisterRequest request = userTestUtil.generateRandomUser().toBuilder()
                .phone(phone)
                .build();
        Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
        assertTrue(response.asString().contains("phone"),
                "Ошибка должна относиться к телефону (" + caseDescription + ")");
        ApiResponseAssert.assertError(
                response,
                ApiError.BAD_REQUEST.getStatus(),
                ApiError.BAD_REQUEST.getCode(),
                PHONE_INVALID.getMsg()
        );
        DbResponseAssert.assertUserNotExistsInDb(appUserRepository, request.getUsername());
    }

    @ParameterizedTest(name = "[{index}] phone=\"{0}\" — {1}")
    @MethodSource("invalidPhoneSpecialCases")
    @DisplayName("Register: phone с emoji/юникодом, XSS или SQL-инъекцией")
    void registerWithSpecialPhoneValues(String phone, String caseDescription) {
        RegisterRequest request = userTestUtil.generateRandomUser().toBuilder()
                .phone(phone)
                .build();
        Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
        ApiResponseAssert.assertErrorPartly(
                response,
                ApiError.BAD_REQUEST.getStatus(),
                ApiError.BAD_REQUEST.getCode(),
                "Телефон должен состоять ровно из 11 цифр;"
        );
        DbResponseAssert.assertUserNotExistsInDb(appUserRepository, request.getUsername());
    }

    // ====== Валидация email ======

    @Test
    @DisplayName("Register: email с двумя точками")
    void registerWithEmailWithTwoPoints() {
        RegisterRequest request = userTestUtil.generateRandomUser().toBuilder()
                .email("test@mail..ru")
                .build();
        String userId = null;
        try {
            Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
            ApiResponseAssert.assertRegisterSuccess(
                    response,
                    request.getEmail(),
                    request.getUsername(),
                    request.getRole(),
                    request.getPhone()
            );
            userId = response.jsonPath().getString(DATA_ID.getPath());
            DbResponseAssert.assertUserCorrectInDb(
                    appUserRepository,
                    request.getUsername(),
                    request.getEmail(),
                    request.getPhone(),
                    USER);
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Register: email нижняя граница (мин длина)")
    void registerWithEmail1LongName() {
        RegisterRequest request = userTestUtil.generateRandomUser().toBuilder()
                .email("1@a.ru")
                .build();
        String userId = null;
        try {
            Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
            ApiResponseAssert.assertRegisterSuccess(
                    response,
                    request.getEmail(),
                    request.getUsername(),
                    request.getRole(),
                    request.getPhone()
            );
            userId = response.jsonPath().getString(DATA_ID.getPath());
            DbResponseAssert.assertUserCorrectInDb(
                    appUserRepository,
                    request.getUsername(),
                    request.getEmail(),
                    request.getPhone(),
                    USER);
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Register: email верхняя граница (255 символов)")
    void registerWithEmail255Long() {
        RegisterRequest request = userTestUtil.generateRandomUser().toBuilder()
                .email("q".repeat(247) + "@mail.ru")
                .build();
        String userId = null;
        try {
            Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
            ApiResponseAssert.assertRegisterSuccess(
                    response,
                    request.getEmail(),
                    request.getUsername(),
                    request.getRole(),
                    request.getPhone()
            );
            userId = response.jsonPath().getString(DATA_ID.getPath());
            DbResponseAssert.assertUserCorrectInDb(
                    appUserRepository,
                    request.getUsername(),
                    request.getEmail(),
                    request.getPhone(),
                    USER);
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Register: email верхняя граница + 1 (256 символов)")
    void registerWithEmail256Long() {
        RegisterRequest request = userTestUtil.generateRandomUser().toBuilder()
                .email("test@" + "q".repeat(248) + ".ru")
                .build();
        Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
        ApiResponseAssert.assertError(
                response,
                ApiError.BAD_REQUEST.getStatus(),
                ApiError.BAD_REQUEST.getCode(),
                EMAIL_LENGTH.getMsg()
        );
        DbResponseAssert.assertUserNotExistsInDb(appUserRepository, request.getUsername());
    }

    @ParameterizedTest(name = "[{index}] email=\"{0}\" — {1}")
    @MethodSource("invalidEmailEmptyCases")
    @DisplayName("Register: email = null/пусто/пробел")
    void registerWithEmptyOrNullEmail(String email, String caseDescription, boolean expectFull) {
        RegisterRequest request = userTestUtil.generateRandomUser().toBuilder()
                .email(email)
                .build();
        Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);

        if (expectFull) {
            ApiResponseAssert.assertError(
                    response,
                    ApiError.BAD_REQUEST.getStatus(),
                    ApiError.BAD_REQUEST.getCode(),
                    EMAIL_EMPTY.getMsg()
            );
        } else {
            ApiResponseAssert.assertErrorPartly(
                    response,
                    ApiError.BAD_REQUEST.getStatus(),
                    ApiError.BAD_REQUEST.getCode(),
                    EMAIL_EMPTY.getMsg()
            );
        }
        DbResponseAssert.assertUserNotExistsInDb(appUserRepository, request.getUsername());
    }

    @ParameterizedTest(name = "[{index}] email=\"{0}\" — {1}")
    @MethodSource("invalidEmailCases")
    @DisplayName("Register: email невалиден (параметризованный)")
    void registerWithInvalidEmail(String email, String caseDescription) {
        RegisterRequest request = userTestUtil.generateRandomUser().toBuilder()
                .email(email)
                .build();
        Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
        ApiResponseAssert.assertError(
                response,
                ApiError.BAD_REQUEST.getStatus(),
                ApiError.BAD_REQUEST.getCode(),
                EMAIL_FORMAT.getMsg()
        );
        assertTrue(response.asString().contains("email"),
                "Ошибка должна относиться к email (" + caseDescription + ")");
        DbResponseAssert.assertUserNotExistsInDb(appUserRepository, request.getUsername());
    }

    @ParameterizedTest(name = "[{index}] email=\"{0}\" — {1}")
    @MethodSource("invalidEmailSpecialCases")
    @DisplayName("Register: email с emoji/юникодом, XSS, SQL-инъекцией")
    void registerWithInvalidSpecialEmail(String email, String caseDescription) {
        RegisterRequest request = userTestUtil.generateRandomUser().toBuilder()
                .email(email)
                .build();
        Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);

        ApiResponseAssert.assertErrorPartly(
                response,
                ApiError.BAD_REQUEST.getStatus(),
                ApiError.BAD_REQUEST.getCode(),
                EMAIL_FORMAT.getMsg()
        );
        DbResponseAssert.assertUserNotExistsInDb(appUserRepository, request.getUsername());
    }

    // ====== Позитивные сценарии ======

    @Test
    @DisplayName("Register USER: успешная регистрация")
    void shouldRegisterUserSuccessfully() {
        RegisterRequest request = userTestUtil.generateRandomUser();
        String userId = null;
        try {
            Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
            ApiResponseAssert.assertRegisterSuccess(
                    response,
                    request.getEmail(),
                    request.getUsername(),
                    request.getRole(),
                    request.getPhone()
            );
            userId = response.jsonPath().getString(DATA_ID.getPath());
            DbResponseAssert.assertUserCorrectInDb(
                    appUserRepository,
                    request.getUsername(),
                    request.getEmail(),
                    request.getPhone(),
                    USER);
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Register ADMIN: успешная регистрация")
    void shouldRegisterAdminSuccessfully() {
        RegisterRequest request = userTestUtil.generateRandomUser().toBuilder()
                .role(ADMIN.getEnName())
                .build();
        String userId = null;
        try {
            Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
            ApiResponseAssert.assertRegisterSuccess(
                    response,
                    request.getEmail(),
                    request.getUsername(),
                    request.getRole(),
                    request.getPhone()
            );
            userId = response.jsonPath().getString(DATA_ID.getPath());
            DbResponseAssert.assertUserCorrectInDb(
                    appUserRepository,
                    request.getUsername(),
                    request.getEmail(),
                    request.getPhone(),
                    ADMIN);
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Register: пробелы в начале и в конце должны быть обрезаны")
    void registerTrimAllFields() {
        String password = "  A1b!Trim  ";
        String email = "   user@mail.ru   ";
        String phone = "   79990009999  ";
        String username = " trimUser ";
        RegisterRequest request = RegisterRequest.builder()
                .username(username)
                .password(password)
                .role(USER.getEnName())
                .phone(phone)
                .email(email)
                .build();
        String userId = null;
        try {
            Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
            ApiResponseAssert.assertRegisterSuccess(
                    response,
                    email.trim(),
                    username.trim(),
                    USER.name(),
                    phone.trim()
            );
            userId = response.jsonPath().getString(DATA_ID.getPath());
            DbResponseAssert.assertUserCorrectInDb(
                    appUserRepository,
                    username.trim(),
                    email.trim(),
                    phone.trim(),
                    USER);
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Register: лишние поля в body (ignored)")
    void registerWithExtraFields() {
        String password = TestDataUtil.validPassword();
        String email = TestDataUtil.randomEmail();
        String phone = TestDataUtil.randomPhone();
        String username = TestDataUtil.randomUsername();
        String shouldBeIgnored = "shouldBeIgnored";
        Map<String, Object> request = new HashMap<>();
        request.put("username", username);
        request.put("password", password);
        request.put("role", USER);
        request.put("phone", phone);
        request.put("email", email);
        request.put("extraField", shouldBeIgnored);
        String userId = null;
        try {
            Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
            ApiResponseAssert.assertRegisterSuccess(
                    response,
                    email,
                    username,
                    USER.name(),
                    phone
            );
            userId = response.jsonPath().getString(DATA_ID.getPath());
            DbResponseAssert.assertUserCorrectInDb(
                    appUserRepository,
                    username,
                    email,
                    phone,
                    USER);
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Register: Custom headers — пользователь успешно зарегистрирован")
    void registerWithCustomHeaders() {
        RegisterRequest request = userTestUtil.generateRandomUser();
        String userId = null;
        try {
            Response response = given()
                    .header("X-Custom-Header", "test")
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .post(AUTH_REGISTER.getPath());
            ApiResponseAssert.assertRegisterSuccess(
                    response,
                    request.getEmail(),
                    request.getUsername(),
                    USER.getEnName(),
                    request.getPhone()
            );
            userId = response.jsonPath().getString(DATA_ID.getPath());
            DbResponseAssert.assertUserCorrectInDb(
                    appUserRepository,
                    request.getUsername(),
                    request.getEmail(),
                    request.getPhone(),
                    USER
            );
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    // ====== Негативные сценарии (конфликты и дубликаты) ======

    @Test
    @DisplayName("Register USER: повторная регистрация с тем же username")
    void registerWithDuplicateUsernameUSER() {
        String username = TestDataUtil.randomUsername();
        RegisterRequest first = userTestUtil.generateRandomUser().toBuilder()
                .username(username)
                .build();
        String userId = null;
        try {
            Response firstResponse = apiHelper.post(AUTH_REGISTER.getPath(), first);
            ApiResponseAssert.assertRegisterSuccess(
                    firstResponse,
                    first.getEmail(),
                    first.getUsername(),
                    first.getRole(),
                    first.getPhone()
            );
            userId = firstResponse.jsonPath().getString(DATA_ID.getPath());
            DbResponseAssert.assertUserCorrectInDb(
                    appUserRepository,
                    first.getUsername(),
                    first.getEmail(),
                    first.getPhone(),
                    USER);
            RegisterRequest second = userTestUtil.generateRandomUser().toBuilder()
                    .username(username)
                    .build();
            Response secondResponse = apiHelper.post(AUTH_REGISTER.getPath(), second);
            ApiResponseAssert.assertError(
                    secondResponse,
                    USER_ALREADY_EXISTS.getStatus(),
                    USER_ALREADY_EXISTS.getCode(),
                    ErrorMessage.USER_ALREADY_EXISTS.getMsg()
            );
            DbResponseAssert.assertUserCountByField(appUserRepository, "username", username, 1);
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Register ADMIN: повторная регистрация с тем же username")
    void registerWithDuplicateUsernameADMIN() {
        String username = TestDataUtil.randomUsername();
        RegisterRequest first = userTestUtil.generateRandomUser().toBuilder()
                .username(username)
                .role(ADMIN.getEnName())
                .build();
        String userId = null;
        try {
            Response firstResponse = apiHelper.post(AUTH_REGISTER.getPath(), first);
            ApiResponseAssert.assertRegisterSuccess(
                    firstResponse,
                    first.getEmail(),
                    first.getUsername(),
                    first.getRole(),
                    first.getPhone()
            );
            userId = firstResponse.jsonPath().getString(DATA_ID.getPath());
            DbResponseAssert.assertUserCorrectInDb(
                    appUserRepository,
                    first.getUsername(),
                    first.getEmail(),
                    first.getPhone(),
                    ADMIN);
            RegisterRequest second = userTestUtil.generateRandomUser().toBuilder()
                    .username(username)
                    .build();
            Response secondResponse = apiHelper.post(AUTH_REGISTER.getPath(), second);
            ApiResponseAssert.assertError(
                    secondResponse,
                    USER_ALREADY_EXISTS.getStatus(),
                    USER_ALREADY_EXISTS.getCode(),
                    ErrorMessage.USER_ALREADY_EXISTS.getMsg()
            );
            DbResponseAssert.assertUserCountByField(appUserRepository, "username", username, 1);
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Register: повторная регистрация с тем же email")
    void registerWithDuplicateEmail() {
        String email = TestDataUtil.randomEmail();
        RegisterRequest first = userTestUtil.generateRandomUser().toBuilder()
                .email(email)
                .build();
        String userIdFirst = null;
        String userIdSecond = null;
        try {
            Response firstResponse = apiHelper.post(AUTH_REGISTER.getPath(), first);
            ApiResponseAssert.assertRegisterSuccess(
                    firstResponse,
                    first.getEmail(),
                    first.getUsername(),
                    first.getRole(),
                    first.getPhone()
            );
            userIdFirst = firstResponse.jsonPath().getString(DATA_ID.getPath());
            DbResponseAssert.assertUserCorrectInDb(
                    appUserRepository,
                    first.getUsername(),
                    first.getEmail(),
                    first.getPhone(),
                    USER);
            RegisterRequest second = userTestUtil.generateRandomUser().toBuilder()
                    .email(email)
                    .build();
            Response secondResponse = apiHelper.post(AUTH_REGISTER.getPath(), second);
            ApiResponseAssert.assertRegisterSuccess(
                    secondResponse,
                    second.getEmail(),
                    second.getUsername(),
                    second.getRole(),
                    second.getPhone()
            );
            userIdSecond = secondResponse.jsonPath().getString(DATA_ID.getPath());
            DbResponseAssert.assertUserCorrectInDb(
                    appUserRepository,
                    second.getUsername(),
                    second.getEmail(),
                    second.getPhone(),
                    USER);
            DbResponseAssert.assertUserCountByField(appUserRepository, "email", email, 2);
        } finally {
            if (userIdFirst != null || userIdSecond != null) {
                userTestUtil.deleteUser(userIdFirst);
                userTestUtil.deleteUser(userIdSecond);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userIdFirst);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userIdSecond);
            }
        }
    }

    @Test
    @DisplayName("Register: повторная регистрация с тем же phone")
    void registerWithDuplicatePhone() {
        String phone = TestDataUtil.randomPhone();
        RegisterRequest first = userTestUtil.generateRandomUser().toBuilder()
                .phone(phone)
                .build();
        String userIdFirst = null;
        String userIdSecond = null;
        try {
            Response firstResponse = apiHelper.post(AUTH_REGISTER.getPath(), first);
            ApiResponseAssert.assertRegisterSuccess(
                    firstResponse,
                    first.getEmail(),
                    first.getUsername(),
                    first.getRole(),
                    first.getPhone()
            );
            userIdFirst = firstResponse.jsonPath().getString(DATA_ID.getPath());
            DbResponseAssert.assertUserCorrectInDb(
                    appUserRepository,
                    first.getUsername(),
                    first.getEmail(),
                    first.getPhone(),
                    USER);
            RegisterRequest second = userTestUtil.generateRandomUser().toBuilder()
                    .phone(phone)
                    .build();
            Response secondResponse = apiHelper.post(AUTH_REGISTER.getPath(), second);
            ApiResponseAssert.assertRegisterSuccess(
                    secondResponse,
                    second.getEmail(),
                    second.getUsername(),
                    second.getRole(),
                    second.getPhone()
            );
            userIdSecond = secondResponse.jsonPath().getString(DATA_ID.getPath());
            DbResponseAssert.assertUserCorrectInDb(
                    appUserRepository,
                    second.getUsername(),
                    second.getEmail(),
                    second.getPhone(),
                    USER);
            DbResponseAssert.assertUserCountByField(appUserRepository, "phone", phone, 2);
        } finally {
            if (userIdFirst != null || userIdSecond != null) {
                userTestUtil.deleteUser(userIdFirst);
                userTestUtil.deleteUser(userIdSecond);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userIdFirst);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userIdSecond);
            }
        }
    }

    @Test
    @DisplayName("Register ADMIN: нельзя зарегистрировать более 3-х админов")
    void shouldNotAllowMoreThanThreeAdmins() {
        int adminsBefore = appUserRepository.countByRole(ADMIN);
        int canCreate = 3 - adminsBefore;
        for (int i = 0; i < canCreate; i++) {
            RegisterRequest request = userTestUtil.generateRandomUser().toBuilder()
                    .role(ADMIN.getEnName())
                    .build();
            Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
            ApiResponseAssert.assertRegisterSuccess(
                    response,
                    request.getEmail(),
                    request.getUsername(),
                    request.getRole(),
                    request.getPhone()
            );
            String userId = response.jsonPath().getString(DATA_ID.getPath());
            createdUserIds.add(userId);
            DbResponseAssert.assertUserCorrectInDb(
                    appUserRepository,
                    request.getUsername(),
                    request.getEmail(),
                    request.getPhone(),
                    ADMIN
            );
        }
        RegisterRequest extraAdmin = userTestUtil.generateRandomUser().toBuilder()
                .role(ADMIN.getEnName())
                .build();
        Response failResponse = apiHelper.post(AUTH_REGISTER.getPath(), extraAdmin);
        ApiResponseAssert.assertError(
                failResponse,
                FORBIDDEN.getStatus(),
                FORBIDDEN.getCode(),
                ADMIN_LIMIT.getMsg()
        );
        DbResponseAssert.assertUserNotExistsInDb(appUserRepository, extraAdmin.getUsername());
        for (String userId : createdUserIds) {
            userTestUtil.deleteUser(userId);
            DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
        }
        createdUserIds.clear();
    }

    // ====== Структура и формат запроса ======

    @Test
    @DisplayName("Register: пустой body \"\"")
    void registerWithEmptyBody() {
        String badRequestJson = "";
        long countBefore = appUserRepository.count();
        Response response = apiHelper.post(AUTH_REGISTER.getPath(), badRequestJson);
        ApiResponseAssert.assertErrorPartly(
                response,
                ApiError.BAD_REQUEST.getStatus(),
                ApiError.BAD_REQUEST.getCode(),
                INVALID_OR_EMPTY_JSON.getMsg()
        );
        long countAfter = appUserRepository.count();
        assertEquals(countBefore, countAfter, "В БД не должно появиться новых пользователей");
    }

    @Test
    @DisplayName("Register: пустой body {}")
    void registerWithEmptyBracketsBody() {
        String badRequestJson = "{}";
        long countBefore = appUserRepository.count();
        Response response = apiHelper.post(AUTH_REGISTER.getPath(), badRequestJson);
        ApiResponseAssert.assertErrorPartly(
                response,
                ApiError.BAD_REQUEST.getStatus(),
                ApiError.BAD_REQUEST.getCode(),
                EMPTY_USERNAME.getMsg()
        );
        long countAfter = appUserRepository.count();
        assertEquals(countBefore, countAfter, "В БД не должно появиться новых пользователей");
    }

    @Test
    @DisplayName("Register: нет body")
    void registerNoBody() {
        long countBefore = appUserRepository.count();
        Response response = RestAssured
                .given()
                .contentType("application/json")
                .post(AUTH_REGISTER.getPath())
                .andReturn();
        assertEquals(ApiError.BAD_REQUEST.getStatus(), response.statusCode());
        assertTrue(response.asString().contains(ApiError.BAD_REQUEST.getCode()), "Должна быть ошибка валидации");
        assertTrue(response.asString().contains(INVALID_OR_EMPTY_JSON.getMsg()), "Должно быть сообщение о пустом JSON");
        long countAfter = appUserRepository.count();
        assertEquals(countBefore, countAfter, "В БД не должно появиться новых пользователей");
    }

    @ParameterizedTest(name = "[{index}] Некорректный JSON: {1}")
    @MethodSource("invalidJsonRequests")
    @DisplayName("Register: невалидный JSON (массив, число, строка, null, нет кавычек/скобки)")
    void registerWithInvalidJson(String badRequestJson, String caseDescription) {
        long countBefore = appUserRepository.count();
        Response response = apiHelper.post(AUTH_REGISTER.getPath(), badRequestJson);
        ApiResponseAssert.assertErrorPartly(
                response,
                ApiError.BAD_REQUEST.getStatus(),
                ApiError.BAD_REQUEST.getCode(),
                INVALID_OR_EMPTY_JSON.getMsg()
        );
        long countAfter = appUserRepository.count();
        assertEquals(countBefore, countAfter, "В БД не должно появиться новых пользователей");
    }

    // ====== Content-Type и заголовки ======

    @Test
    @DisplayName("Register: Content-Type отсутствует")
    void registerNoContentType() {
        RegisterRequest request = userTestUtil.generateRandomUser();
        long countBefore = appUserRepository.count();
        Response response = given()
                .body(request)
                .when()
                .post(AUTH_REGISTER.getPath());
        assertEquals(UNSUPPORTED_MEDIA_TYPE.getStatus(), response.statusCode());
        assertTrue(response.asString().contains(UNSUPPORTED_MEDIA_TYPE.getCode()), "Должна быть ошибка валидации");
        assertTrue(response.asString().contains(ErrorMessage.UNSUPPORTED_MEDIA_TYPE.getMsg()),
                "Должно быть сообщение о неподдерживаемом Content-Type");
        long countAfter = appUserRepository.count();
        assertEquals(countBefore, countAfter, "В БД не должно появиться новых пользователей");
    }

    @Test
    @DisplayName("Register: Content-Type = text/plain")
    void registerTextPlain() {
        long countBefore = appUserRepository.count();
        Response response = given()
                .contentType("text/plain")
                .body("{\"request\"}")
                .when()
                .post(AUTH_REGISTER.getPath());
        assertEquals(UNSUPPORTED_MEDIA_TYPE.getStatus(), response.statusCode());
        assertTrue(response.asString().contains(UNSUPPORTED_MEDIA_TYPE.getCode()), "Должна быть ошибка валидации");
        assertTrue(response.asString().contains(ErrorMessage.UNSUPPORTED_MEDIA_TYPE.getMsg()),
                "Должно быть сообщение о неподдерживаемом Content-Type");
        long countAfter = appUserRepository.count();
        assertEquals(countBefore, countAfter, "В БД не должно появиться новых пользователей");
    }

    @Test
    @DisplayName("Register: Content-Type = application/javascript")
    void registerJs() {
        long countBefore = appUserRepository.count();
        Response response = given()
                .contentType("text/javascript")
                .body("var x = 1;")
                .when()
                .post(AUTH_REGISTER.getPath());
        assertEquals(UNSUPPORTED_MEDIA_TYPE.getStatus(), response.statusCode());
        assertTrue(response.asString().contains(UNSUPPORTED_MEDIA_TYPE.getCode()), "Должна быть ошибка валидации");
        assertTrue(response.asString().contains(ErrorMessage.UNSUPPORTED_MEDIA_TYPE.getMsg()),
                "Должно быть сообщение о неподдерживаемом Content-Type");
        long countAfter = appUserRepository.count();
        assertEquals(countBefore, countAfter, "В БД не должно появиться новых пользователей");
    }

    @Test
    @DisplayName("Register: Content-Type = application/xml")
    void registerXml() {
        long countBefore = appUserRepository.count();
        Response response = given()
                .contentType("text/xml")
                .body("<user><username>xml</username></user>")
                .when()
                .post(AUTH_REGISTER.getPath());
        assertEquals(UNSUPPORTED_MEDIA_TYPE.getStatus(), response.statusCode());
        assertTrue(response.asString().contains(UNSUPPORTED_MEDIA_TYPE.getCode()), "Должна быть ошибка валидации");
        assertTrue(response.asString().contains(ErrorMessage.UNSUPPORTED_MEDIA_TYPE.getMsg()),
                "Должно быть сообщение о неподдерживаемом Content-Type");
        long countAfter = appUserRepository.count();
        assertEquals(countBefore, countAfter, "В БД не должно появиться новых пользователей");
    }

    @Test
    @DisplayName("Register: Content-Type = text/html")
    void registerHtml() {
        long countBefore = appUserRepository.count();
        Response response = given()
                .contentType("text/html")
                .body("<div>hello</div>")
                .when()
                .post(AUTH_REGISTER.getPath());
        assertEquals(UNSUPPORTED_MEDIA_TYPE.getStatus(), response.statusCode());
        assertTrue(response.asString().contains(UNSUPPORTED_MEDIA_TYPE.getCode()), "Должна быть ошибка валидации");
        assertTrue(response.asString().contains(ErrorMessage.UNSUPPORTED_MEDIA_TYPE.getMsg()),
                "Должно быть сообщение о неподдерживаемом Content-Type");
        long countAfter = appUserRepository.count();
        assertEquals(countBefore, countAfter, "В БД не должно появиться новых пользователей");
    }

    @Test
    @DisplayName("Register: Content-Type = multipart/form-data")
    void registerMultipart() {
        long countBefore = appUserRepository.count();
        Response response = given()
                .multiPart("file", "filename.txt", "some text".getBytes())
                .when()
                .post(AUTH_REGISTER.getPath());
        assertEquals(UNSUPPORTED_MEDIA_TYPE.getStatus(), response.statusCode());
        assertTrue(response.asString().contains(UNSUPPORTED_MEDIA_TYPE.getCode()), "Должна быть ошибка валидации");
        assertTrue(response.asString().contains(ErrorMessage.UNSUPPORTED_MEDIA_TYPE.getMsg()),
                "Должно быть сообщение о неподдерживаемом Content-Type");
        long countAfter = appUserRepository.count();
        assertEquals(countBefore, countAfter, "В БД не должно появиться новых пользователей");
    }

    @Test
    @DisplayName("Register: Content-Type = application/x-www-form-urlencoded")
    void registerUrlEncoded() {
        long countBefore = appUserRepository.count();
        Response response = given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("username", "user")
                .formParam("password", "A1b!test")
                .formParam("role", USER.getEnName())
                .formParam("phone", "79990000001")
                .formParam("email", "user@mail.ru")
                .when()
                .post(AUTH_REGISTER.getPath());
        assertEquals(UNSUPPORTED_MEDIA_TYPE.getStatus(), response.statusCode());
        assertTrue(response.asString().contains(UNSUPPORTED_MEDIA_TYPE.getCode()), "Должна быть ошибка валидации");
        assertTrue(response.asString().contains(ErrorMessage.UNSUPPORTED_MEDIA_TYPE.getMsg()),
                "Должно быть сообщение о неподдерживаемом Content-Type");
        long countAfter = appUserRepository.count();
        assertEquals(countBefore, countAfter, "В БД не должно появиться новых пользователей");
    }

    @Test
    @DisplayName("Register: JSON с Content-Type text/plain")
    void registerJsonWithTextPlain() {
        String request = "{"
                + "\"username\": \"" + TestDataUtil.randomUsername() + "\","
                + "\"password\": \"" + TestDataUtil.validPassword() + "\","
                + "\"role\": \"" + USER.getEnName() + "\","
                + "\"phone\": \"" + TestDataUtil.randomPhone() + "\","
                + "\"email\": \"" + TestDataUtil.randomEmail() + "\""
                + "}";
        long countBefore = appUserRepository.count();
        Response response = given()
                .contentType("text/plain")
                .body(request)
                .when()
                .post(AUTH_REGISTER.getPath());
        assertEquals(UNSUPPORTED_MEDIA_TYPE.getStatus(), response.statusCode());
        assertTrue(response.asString().contains(UNSUPPORTED_MEDIA_TYPE.getCode()), "Должна быть ошибка валидации");
        assertTrue(response.asString().contains(ErrorMessage.UNSUPPORTED_MEDIA_TYPE.getMsg()),
                "Должно быть сообщение о неподдерживаемом Content-Type");
        long countAfter = appUserRepository.count();
        assertEquals(countBefore, countAfter, "В БД не должно появиться новых пользователей");
    }

    // ====== HTTP-метод и endpoint ======

    @Test
    @DisplayName("Register: GET вместо POST")
    void registerGetInsteadOfPost() {
        String get = "GET";
        RegisterRequest request = userTestUtil.generateRandomUser();
        long countBefore = appUserRepository.count();
        Response response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .get(AUTH_REGISTER.getPath());
        ApiResponseAssert.assertError(
                response,
                METHOD_NOT_ALLOWED.getStatus(),
                METHOD_NOT_ALLOWED.getCode(),
                String.format(ErrorMessage.METHOD_NOT_ALLOWED.getMsg(), get)
        );
        long countAfter = appUserRepository.count();
        assertEquals(countBefore, countAfter, "В БД не должно появиться новых пользователей");
    }

    @Test
    @DisplayName("Register: PUT вместо POST")
    void registerPutInsteadOfPost() {
        String put = "PUT";
        RegisterRequest request = userTestUtil.generateRandomUser();
        long countBefore = appUserRepository.count();
        Response response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(AUTH_REGISTER.getPath());
        ApiResponseAssert.assertError(
                response,
                METHOD_NOT_ALLOWED.getStatus(),
                METHOD_NOT_ALLOWED.getCode(),
                String.format(ErrorMessage.METHOD_NOT_ALLOWED.getMsg(), put)
        );
        long countAfter = appUserRepository.count();
        assertEquals(countBefore, countAfter, "В БД не должно появиться новых пользователей");
    }

    @Test
    @DisplayName("Register: DELETE вместо POST")
    void registerDeleteInsteadOfPost() {
        String delete = "DELETE";
        RegisterRequest request = userTestUtil.generateRandomUser();
        long countBefore = appUserRepository.count();
        Response response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .delete(AUTH_REGISTER.getPath());
        ApiResponseAssert.assertError(
                response,
                METHOD_NOT_ALLOWED.getStatus(),
                METHOD_NOT_ALLOWED.getCode(),
                String.format(ErrorMessage.METHOD_NOT_ALLOWED.getMsg(), delete)
        );
        long countAfter = appUserRepository.count();
        assertEquals(countBefore, countAfter, "В БД не должно появиться новых пользователей");
    }

    @Test
    @DisplayName("Register: несуществующий endpoint")
    void registerNotExistingEndpoint() {
        RegisterRequest request = userTestUtil.generateRandomUser();
        long countBefore = appUserRepository.count();
        Response response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(AUTH_REGISTER_NOT_EXIST.getPath());
        ApiResponseAssert.assertError(
                response,
                NOT_FOUND.getStatus(),
                NOT_FOUND.getCode(),
                ENDPOINT_NOT_FOUND.getMsg()
        );
        long countAfter = appUserRepository.count();
        assertEquals(countBefore, countAfter, "В БД не должно появиться новых пользователей");
    }

    // ====== HTTP-ответ (meta) ======

    @Test
    @DisplayName("Register: Ответ не содержит Set-Cookie")
    void registerNoSetCookieInResponse() {
        RegisterRequest request = userTestUtil.generateRandomUser();
        String userId = null;
        try {
            Response response = given()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .post(AUTH_REGISTER.getPath())
                    .then()
                    .statusCode(OK.getStatus())
                    .header(ApiHeader.SET_COOKIE.getTitle(), nullValue())
                    .extract().response();
            ApiResponseAssert.assertRegisterSuccess(
                    response,
                    request.getEmail(),
                    request.getUsername(),
                    USER.getEnName(),
                    request.getPhone()
            );
            userId = response.jsonPath().getString(DATA_ID.getPath());
            DbResponseAssert.assertUserCorrectInDb(
                    appUserRepository,
                    request.getUsername(),
                    request.getEmail(),
                    request.getPhone(),
                    USER
            );
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Register: Cache-Control и Pragma")
    void registerCacheControlHeaders() {
        RegisterRequest request = userTestUtil.generateRandomUser();
        String userId = null;
        try {
            Response response = given()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .post(AUTH_REGISTER.getPath())
                    .then()
                    .statusCode(OK.getStatus())
                    .header(ApiHeader.CACHE_CONTROL.getTitle(), notNullValue())
                    .header(ApiHeader.PRAGMA.getTitle(), notNullValue())
                    .extract().response();
            ApiResponseAssert.assertRegisterSuccess(
                    response,
                    request.getEmail(),
                    request.getUsername(),
                    USER.getEnName(),
                    request.getPhone()
            );
            userId = response.jsonPath().getString(DATA_ID.getPath());
            DbResponseAssert.assertUserCorrectInDb(
                    appUserRepository,
                    request.getUsername(),
                    request.getEmail(),
                    request.getPhone(),
                    USER
            );
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Register: Время ответа <2 сек (200 OK)")
    void registerResponseTime200() {
        RegisterRequest request = userTestUtil.generateRandomUser();
        String userId = null;
        try {
            Response response = given()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .post(AUTH_REGISTER.getPath())
                    .then()
                    .time(lessThan(2000L))
                    .statusCode(OK.getStatus())
                    .extract().response();
            ApiResponseAssert.assertRegisterSuccess(
                    response,
                    request.getEmail(),
                    request.getUsername(),
                    USER.getEnName(),
                    request.getPhone()
            );
            userId = response.jsonPath().getString(DATA_ID.getPath());
            DbResponseAssert.assertUserCorrectInDb(
                    appUserRepository,
                    request.getUsername(),
                    request.getEmail(),
                    request.getPhone(),
                    USER
            );
        } finally {
            if (userId != null) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Register: Время ответа <2 сек (400 Bad Request)")
    void registerResponseTime400() {
        String badRequestJson = "";
        long countBefore = appUserRepository.count();
        given()
                .contentType(ContentType.JSON)
                .body(badRequestJson)
                .when()
                .post(AUTH_REGISTER.getPath())
                .then()
                .time(lessThan(2000L))
                .statusCode(ApiError.BAD_REQUEST.getStatus());
        long countAfter = appUserRepository.count();
        assertEquals(countBefore, countAfter, "В БД не должно появиться новых пользователей");
    }

    @Test
    @DisplayName("Register: Время ответа <2 сек (415 Unsupported Media Type)")
    void registerResponseTime415() {
        String badRequestJson = "";
        long countBefore = appUserRepository.count();
        given()
                .contentType("text/plain")
                .body(badRequestJson)
                .when()
                .post(AUTH_REGISTER.getPath())
                .then()
                .time(lessThan(2000L))
                .statusCode(UNSUPPORTED_MEDIA_TYPE.getStatus());
        long countAfter = appUserRepository.count();
        assertEquals(countBefore, countAfter, "В БД не должно появиться новых пользователей");
    }

    @Test
    @DisplayName("Register: Время ответа <2 сек (404 Not Found)")
    void registerResponseTime404() {
        String badRequestJson = "";
        long countBefore = appUserRepository.count();
        given()
                .contentType(ContentType.JSON)
                .body(badRequestJson)
                .when()
                .post("/auth/not_found_endpoint")
                .then()
                .time(lessThan(2000L))
                .statusCode(NOT_FOUND.getStatus());
        long countAfter = appUserRepository.count();
        assertEquals(countBefore, countAfter, "В БД не должно появиться новых пользователей");
    }

    @Test
    @DisplayName("Register: Время ответа <2 сек (405 Method not allowed)")
    void registerResponseTime405() {
        String badRequestJson = "";
        long countBefore = appUserRepository.count();
        given()
                .contentType(ContentType.JSON)
                .body(badRequestJson)
                .when()
                .get(AUTH_REGISTER.getPath())
                .then()
                .time(lessThan(2000L))
                .statusCode(METHOD_NOT_ALLOWED.getStatus());
        long countAfter = appUserRepository.count();
        assertEquals(countBefore, countAfter, "В БД не должно появиться новых пользователей");
    }

    @Test
    @DisplayName("Register: Flood/Rate limit (5+ подряд регистраций, все успешны)")
    void registerFloodNoRateLimit() {
        List<String> createdUserIds = new ArrayList<>();
        try {
            for (int i = 1; i <= 7; i++) {
                RegisterRequest request = userTestUtil.generateRandomUser();
                Response response = apiHelper.post(AUTH_REGISTER.getPath(), request);
                ApiResponseAssert.assertRegisterSuccess(
                        response,
                        request.getEmail(),
                        request.getUsername(),
                        request.getRole(),
                        request.getPhone()
                );
                String userId = response.jsonPath().getString(DATA_ID.getPath());
                createdUserIds.add(userId);
                DbResponseAssert.assertUserCorrectInDb(
                        appUserRepository,
                        request.getUsername(),
                        request.getEmail(),
                        request.getPhone(),
                        USER
                );
            }
        } finally {
            for (String userId : createdUserIds) {
                userTestUtil.deleteUser(userId);
                DbResponseAssert.assertUserDeletedInDb(appUserRepository, userId);
            }
        }
    }

    @Test
    @DisplayName("Register: CORS preflight OPTIONS возвращает 200 и CORS-заголовки")
    void loginCorsOptionsRequest() {
        String origin = "http://localhost:3000";
        String method = "POST";
        given()
                .header("Origin", origin)
                .header("Access-Control-Request-Method", method)
                .when()
                .options(AUTH_REGISTER.getPath())
                .then()
                .statusCode(OK.getStatus())
                .header(ApiHeader.ACCESS_CONTROL_ALLOW_ORIGIN.getTitle(), equalTo(origin))
                .header(ApiHeader.ACCESS_CONTROL_ALLOW_METHODS.getTitle(), equalTo(method))
                .header(ApiHeader.ACCESS_CONTROL_ALLOW_CREDENTIALS.getTitle(), "true");
    }
}

/*
 * ЧЕКЛИСТ регистрации /auth/register (positive + negative)
 *
 * ====== Валидация username ======
 * [ ] Register: username = пусто / пробел / null   (параметризованный)
 * [ ] Register: username = int вместо строки
 * [ ] Register: username = int с лидирующим нулём
 * [ ] Register: username = 1 символ (невалид)
 * [ ] Register: username = 2 символа (валид)
 * [ ] Register: username = 32 символа (валид)
 * [ ] Register: username = 33 символа (невалид)
 * [ ] Register: username с emoji/юникодом / XSS-инъекцией / SQL-инъекцией   (параметризованный)
 * [ ] Register: регистрация с разным регистром username
 *
 * ====== Валидация password ======
 * [ ] Register: password = пусто / пробел / null   (параметризованный)
 * [ ] Register: password = int вместо строки
 * [ ] Register: password = int с лидирующим нулём
 * [ ] Register: password = 5 символов (невалид)
 * [ ] Register: password = 6 символов (валид)
 * [ ] Register: password = 64 символа (валид)
 * [ ] Register: password = 65 символов (невалид)
 * [ ] Register: password невалиден (параметризованный: отсутствие заглавной, маленькой, цифры, спецсимвола, кириллица, пробел и т.п.)
 * [ ] Register: password с emoji/юникодом / XSS-инъекцией / SQL-инъекцией   (параметризованный)
 *
 * ====== Валидация role ======
 * [ ] Register: role = буквы разных регистров
 * [ ] Register: role = маленькими буквами
 * [ ] Register: role = другое значение
 * [ ] Register: невалидная роль (параметризованный: пустая строка, пробел, null)
 * [ ] Register: role = int вместо строки
 * [ ] Register: role с emoji/юникодом / XSS-инъекцией / SQL-инъекцией   (параметризованный)
 *
 * ====== Валидация phone ======
 * [ ] Register: phone = null / пусто / пробел   (параметризованный)
 * [ ] Register: phone невалиден (параметризованный: 10/12 цифр, буквы, скобки, ...)
 * [ ] Register: phone с emoji/юникодом / XSS-инъекцией / SQL-инъекцией   (параметризованный)
 *
 * ====== Валидация email ======
 * [ ] Register: email с двумя точками
 * [ ] Register: email нижняя граница (мин длина)
 * [ ] Register: email верхняя граница (255 символов)
 * [ ] Register: email верхняя граница + 1 (256 символов)
 * [ ] Register: email = null / пусто / пробел   (параметризованный)
 * [ ] Register: email невалиден (параметризованный: все граничные невалидные кейсы)
 * [ ] Register: email с emoji/юникодом / XSS-инъекцией / SQL-инъекцией   (параметризованный)
 *
 * ====== Позитивные сценарии ======
 * [ ] Register USER: успешная регистрация
 * [ ] Register ADMIN: успешная регистрация
 * [ ] Register: пробелы в начале и в конце должны быть обрезаны
 * [ ] Register: лишние поля в body (ignored)
 * [ ] Register: Custom headers — пользователь успешно зарегистрирован
 *
 * ====== Негативные сценарии (конфликты и дубликаты) ======
 * [ ] Register USER: повторная регистрация с тем же username
 * [ ] Register ADMIN: повторная регистрация с тем же username
 * [ ] Register: повторная регистрация с тем же email
 * [ ] Register: повторная регистрация с тем же phone
 * [ ] Register ADMIN: нельзя зарегистрировать более 3-х админов
 *
 * ====== Структура и формат запроса ======
 * [ ] Register: пустой body ""
 * [ ] Register: пустой body {}
 * [ ] Register: нет body
 * [ ] Register: массив вместо объекта
 * [ ] Register: число вместо объекта
 * [ ] Register: строка вместо объекта
 * [ ] Register: null вместо объекта
 * [ ] Register: невалидный JSON (нет кавычек, нет скобки)   (параметризованный)
 *
 * ====== Content-Type и заголовки ======
 * [ ] Register: Content-Type отсутствует
 * [ ] Register: Content-Type = text/plain
 * [ ] Register: Content-Type = application/javascript
 * [ ] Register: Content-Type = application/xml
 * [ ] Register: Content-Type = text/html
 * [ ] Register: Content-Type = multipart/form-data
 * [ ] Register: Content-Type = application/x-www-form-urlencoded
 * [ ] Register: JSON с Content-Type text/plain
 *
 * ====== HTTP-метод и endpoint ======
 * [ ] Register: GET вместо POST
 * [ ] Register: PUT вместо POST
 * [ ] Register: DELETE вместо POST
 * [ ] Register: несуществующий endpoint
 *
 * ====== HTTP-ответ (meta) ======
 * [ ] Register: Ответ не содержит Set-Cookie
 * [ ] Register: Cache-Control и Pragma
 * [ ] Register: Время ответа <2 сек (200 OK)
 * [ ] Register: Время ответа <2 сек (400 Bad Request)
 * [ ] Register: Время ответа <2 сек (415 Unsupported Media Type)
 * [ ] Register: Время ответа <2 сек (404 Not Found)
 * [ ] Register: Время ответа <2 сек (405 Method not allowed)
 * [ ] Register: Flood/Rate limit (5+ подряд регистраций, все успешны)
 * [ ] Register: CORS preflight OPTIONS возвращает 200 и CORS-заголовки
 */