package bookShop.apiTests.common;

import bookShop.apiTests.util.ApiHelper;
import bookShop.apiTests.util.UserTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import io.restassured.RestAssured;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTest {

    @LocalServerPort
    private int port;

    protected ApiHelper apiHelper;
    protected UserTestUtil userTestUtil;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        apiHelper = new ApiHelper();
        userTestUtil = new UserTestUtil(apiHelper);
    }
}