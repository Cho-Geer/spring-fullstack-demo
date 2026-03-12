package com.demo.backend.unit;

import com.demo.backend.config.CookieConfig;
import com.demo.backend.controllers.AuthController;
import com.demo.backend.controllers.AuthController.LoginRequest;
import com.demo.backend.controllers.AuthController.RefreshTokenRequest;
import com.demo.backend.services.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 认证功能测试
 * 测试核心功能，避免复杂的模拟
 */
class AuthFunctionTest {

    @Mock
    private AuthService authService;

    @Mock
    private CookieConfig cookieConfig;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(cookieConfig.isSecure()).thenReturn(false);
        when(cookieConfig.getSameSite()).thenReturn("Lax");
    }

    /**
     * 测试登录功能
     */
    @Test
    void testLogin() {
        // 准备测试数据
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        // 模拟AuthService
        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("success", true);
        serviceResponse.put("accessToken", "access-token");
        serviceResponse.put("refreshToken", "refresh-token");
        serviceResponse.put("username", "testuser");
        when(authService.login(any(LoginRequest.class))).thenReturn(serviceResponse);

        // 执行测试
        // HttpServletResponse response = mock(HttpServletResponse.class); // 不再需要
        ResponseEntity<Map<String, Object>> result = authController.login(loginRequest);

        // 验证结果
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().containsKey("accessToken"));
        assertFalse(result.getBody().containsKey("refreshToken")); // refreshToken应该被移除
        // verify(response).addCookie(any(Cookie.class)); // 不再通过response添加cookie
        assertTrue(result.getHeaders().containsKey(org.springframework.http.HttpHeaders.SET_COOKIE)); // 验证Set-Cookie头
    }

    /**
     * 测试登出功能
     */
    @Test
    void testLogout() {
        // 模拟AuthService
        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("success", true);
        when(authService.logout(any(HttpServletRequest.class))).thenReturn(serviceResponse);

        // 执行测试
        HttpServletRequest request = mock(HttpServletRequest.class);
        // HttpServletResponse response = mock(HttpServletResponse.class); // 不再需要
        ResponseEntity<Map<String, Object>> result = authController.logout(request);

        // 验证结果
        assertEquals(HttpStatus.OK, result.getStatusCode());
        // verify(response).addCookie(any(Cookie.class)); // 不再通过response添加cookie
        assertTrue(result.getHeaders().containsKey(org.springframework.http.HttpHeaders.SET_COOKIE)); // 验证Set-Cookie头
    }

    /**
     * 测试Token刷新功能
     */
    @Test
    void testRefreshToken() {
        // 模拟请求中的Cookie
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie refreshTokenCookie = new Cookie("refreshToken", "old-refresh-token");
        when(request.getCookies()).thenReturn(new Cookie[]{refreshTokenCookie});

        // 模拟AuthService
        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("success", true);
        serviceResponse.put("accessToken", "new-access-token");
        serviceResponse.put("refreshToken", "new-refresh-token");
        when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(serviceResponse);

        // 执行测试
        // HttpServletResponse response = mock(HttpServletResponse.class); // 不再需要
        ResponseEntity<Map<String, Object>> result = authController.refreshToken(request);

        // 验证结果
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().containsKey("accessToken"));
        assertFalse(result.getBody().containsKey("refreshToken")); // refreshToken应该被移除
        // verify(response).addCookie(any(Cookie.class)); // 不再通过response添加cookie
        assertTrue(result.getHeaders().containsKey(org.springframework.http.HttpHeaders.SET_COOKIE)); // 验证Set-Cookie头
    }

    /**
     * 测试登录失败情况
     */
    @Test
    void testLoginFailure() {
        // 准备测试数据
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrong-password");

        // 模拟AuthService
        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("success", false);
        serviceResponse.put("errorCode", "INVALID_CREDENTIALS");
        serviceResponse.put("message", "登录失败：用户名或密码错误");
        when(authService.login(any(LoginRequest.class))).thenReturn(serviceResponse);

        // 执行测试
        // HttpServletResponse response = mock(HttpServletResponse.class); // 不再需要
        ResponseEntity<Map<String, Object>> result = authController.login(loginRequest);

        // 验证结果
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertFalse(result.getBody().containsKey("accessToken"));
    }

    /**
     * 测试刷新Token失败情况
     */
    @Test
    void testRefreshTokenFailure() {
        // 模拟请求中的Cookie
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie refreshTokenCookie = new Cookie("refreshToken", "invalid-refresh-token");
        when(request.getCookies()).thenReturn(new Cookie[]{refreshTokenCookie});

        // 模拟AuthService
        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("success", false);
        serviceResponse.put("errorCode", "INVALID_REFRESH_TOKEN");
        serviceResponse.put("message", "刷新token无效或已过期");
        when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(serviceResponse);

        // 执行测试
        // HttpServletResponse response = mock(HttpServletResponse.class); // 不再需要
        ResponseEntity<Map<String, Object>> result = authController.refreshToken(request);

        // 验证结果
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertFalse(result.getBody().containsKey("accessToken"));
        // verify(response).addCookie(any(Cookie.class)); // 不再通过response添加cookie
        assertTrue(result.getHeaders().containsKey(org.springframework.http.HttpHeaders.SET_COOKIE)); // 验证Set-Cookie头
    }
}
