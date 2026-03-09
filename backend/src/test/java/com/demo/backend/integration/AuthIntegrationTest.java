package com.demo.backend.integration;

import com.demo.backend.controllers.AuthController;
import com.demo.backend.entities.UserEntity;
import com.demo.backend.repositories.UserRepository;
import com.demo.backend.services.SessionManagementService;
import com.demo.backend.utils.JwtUtils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 认证集成测试
 * 使用TestContainers验证完整的认证流程
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private SessionManagementService sessionManagementService;

    // MySQL容器
    @Container
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("demo")
            .withUsername("root")
            .withPassword("password");

    // Redis容器
    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.0"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.redis.host", redisContainer::getHost);
        registry.add("spring.redis.port", redisContainer::getFirstMappedPort);
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        // 清理测试数据
        userRepository.deleteAll();
        // 创建测试用户
        UserEntity user = new UserEntity();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setEnabled(true);
        userRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    /**
     * 测试登录功能
     */
    @Test
    void testLogin() {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "testuser");
        loginRequest.put("password", "password123");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .post("/auth/login");

        response.then().statusCode(HttpStatus.OK.value());
        assertTrue(response.jsonPath().getBoolean("success"));
        assertNotNull(response.jsonPath().getString("accessToken"));
        // 验证refreshToken是否通过Cookie设置
        String refreshToken = response.getCookie("refreshToken");
        assertNotNull(refreshToken);
    }

    /**
     * 测试登出功能
     */
    @Test
    void testLogout() {
        // 先登录获取token
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "testuser");
        loginRequest.put("password", "password123");

        Response loginResponse = given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .post("/auth/login");

        String accessToken = loginResponse.jsonPath().getString("accessToken");
        String refreshToken = loginResponse.getCookie("refreshToken");

        // 登出
        Response logoutResponse = given()
                .header("Authorization", "Bearer " + accessToken)
                .cookie("refreshToken", refreshToken)
                .post("/auth/logout");

        logoutResponse.then().statusCode(HttpStatus.OK.value());
        assertTrue(logoutResponse.jsonPath().getBoolean("success"));
    }

    /**
     * 测试Token刷新功能
     */
    @Test
    void testRefreshToken() {
        // 先登录获取token
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "testuser");
        loginRequest.put("password", "password123");

        Response loginResponse = given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .post("/auth/login");

        String originalAccessToken = loginResponse.jsonPath().getString("accessToken");
        String refreshToken = loginResponse.getCookie("refreshToken");

        // 刷新token
        Response refreshResponse = given()
                .cookie("refreshToken", refreshToken)
                .post("/auth/refresh");

        refreshResponse.then().statusCode(HttpStatus.OK.value());
        assertTrue(refreshResponse.jsonPath().getBoolean("success"));
        String newAccessToken = refreshResponse.jsonPath().getString("accessToken");
        assertNotNull(newAccessToken);
        assertNotEquals(originalAccessToken, newAccessToken);
        // 验证新的refreshToken是否通过Cookie设置
        String newRefreshToken = refreshResponse.getCookie("refreshToken");
        assertNotNull(newRefreshToken);
        assertNotEquals(refreshToken, newRefreshToken);
    }

    /**
     * 测试Token黑名单功能
     */
    @Test
    void testTokenBlacklist() {
        // 先登录获取token
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "testuser");
        loginRequest.put("password", "password123");

        Response loginResponse = given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .post("/auth/login");

        String accessToken = loginResponse.jsonPath().getString("accessToken");
        String refreshToken = loginResponse.getCookie("refreshToken");

        // 登出（会将token添加到黑名单）
        given()
                .header("Authorization", "Bearer " + accessToken)
                .cookie("refreshToken", refreshToken)
                .post("/auth/logout");

        // 尝试使用已登出的token访问受保护的资源
        Response protectedResponse = given()
                .header("Authorization", "Bearer " + accessToken)
                .get("/test/protected");

        protectedResponse.then().statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    /**
     * 测试跨域请求
     */
    @Test
    void testCors() {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "testuser");
        loginRequest.put("password", "password123");

        Response response = given()
                .contentType(ContentType.JSON)
                .header("Origin", "http://localhost:3000")
                .body(loginRequest)
                .post("/auth/login");

        response.then().statusCode(HttpStatus.OK.value());
        // 验证CORS响应头
        assertEquals("*", response.getHeader("Access-Control-Allow-Origin"));
        assertTrue(response.getHeader("Access-Control-Allow-Credentials").equals("true"));
    }
}
