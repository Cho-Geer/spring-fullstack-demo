package com.demo.mysql_jpa_01.security;

import com.demo.mysql_jpa_01.services.SessionManagementService;
import com.demo.mysql_jpa_01.utils.JwtUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private SessionManagementService sessionManagementService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldNotFilter_ApiAuthPath_ReturnsTrue() throws ServletException {
        // 设置请求路径为/api/auth/login，上下文路径为/api
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getContextPath()).thenReturn("/api");

        // 测试shouldNotFilter方法
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        // 验证结果为true（应该跳过过滤器）
        assertTrue(result, "对于/api/auth/login路径，shouldNotFilter应该返回true");
    }

    @Test
    void shouldNotFilter_ApiPublicPath_ReturnsTrue() throws ServletException {
        // 设置请求路径为/api/public/test，上下文路径为/api
        when(request.getRequestURI()).thenReturn("/api/public/test");
        when(request.getContextPath()).thenReturn("/api");

        // 测试shouldNotFilter方法
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        // 验证结果为true（应该跳过过滤器）
        assertTrue(result, "对于/api/public/test路径，shouldNotFilter应该返回true");
    }

    @Test
    void shouldNotFilter_RootPath_ReturnsTrue() throws ServletException {
        // 设置请求路径为/api，上下文路径为/api
        // 这种情况下，去除上下文路径后的路径是/，应该跳过过滤器
        when(request.getRequestURI()).thenReturn("/api");
        when(request.getContextPath()).thenReturn("/api");

        // 测试shouldNotFilter方法
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        // 验证结果为true（应该跳过过滤器）
        assertTrue(result, "对于根路径/，shouldNotFilter应该返回true");
    }

    @Test
    void shouldNotFilter_ProtectedPath_ReturnsFalse() throws ServletException {
        // 设置请求路径为/api/protected/resource，上下文路径为/api
        when(request.getRequestURI()).thenReturn("/api/protected/resource");
        when(request.getContextPath()).thenReturn("/api");

        // 测试shouldNotFilter方法
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        // 验证结果为false（不应该跳过过滤器）
        assertFalse(result, "对于/api/protected/resource路径，shouldNotFilter应该返回false");
    }
    
    @Test
    void shouldNotFilter_WithoutContextPath_WorksCorrectly() throws ServletException {
        // 设置请求路径为/auth/login，无上下文路径
        when(request.getRequestURI()).thenReturn("/auth/login");
        when(request.getContextPath()).thenReturn("");

        // 测试shouldNotFilter方法
        boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

        // 验证结果为true（应该跳过过滤器）
        assertTrue(result, "对于/auth/login路径（无上下文路径），shouldNotFilter应该返回true");
    }
}