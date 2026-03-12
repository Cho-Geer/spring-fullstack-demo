package com.demo.backend.services;

import com.demo.backend.controllers.AuthController.LoginRequest;
import com.demo.backend.controllers.AuthController.RefreshTokenRequest;
import com.demo.backend.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private SessionManagementService sessionManagementService;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = User.withUsername("testuser").password("password").roles("USER").build();
    }

    @Test
    void login_success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        when(jwtUtils.generateToken(userDetails)).thenReturn("accessToken");
        when(jwtUtils.generateRefreshToken(userDetails)).thenReturn("refreshToken");
        when(jwtUtils.getExpirationTime()).thenReturn(3600000L); // 1 hour

        when(sessionManagementService.registerUserToken("testuser", "accessToken", 3600L)).thenReturn(true);
        when(sessionManagementService.getUserActiveSessionCount("testuser")).thenReturn(1);

        Map<String, Object> response = authService.login(request);

        assertTrue((Boolean) response.get("success"));
        assertEquals("登录成功", response.get("message"));
        assertEquals("accessToken", response.get("accessToken"));
        assertEquals("refreshToken", response.get("refreshToken"));
        assertEquals(3600L, response.get("expiresIn"));
        assertEquals(1, response.get("activeSessionCount"));
    }

    @Test
    void login_failure_invalidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrong");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        Map<String, Object> response = authService.login(request);

        assertFalse((Boolean) response.get("success"));
        assertEquals("登录失败：用户名或密码错误", response.get("message"));
        assertEquals("INVALID_CREDENTIALS", response.get("errorCode"));
    }

    @Test
    void logout_success() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer token");

        doNothing().when(sessionManagementService).removeUserToken("token");

        Map<String, Object> response = authService.logout(request);

        assertTrue((Boolean) response.get("success"));
        assertEquals("登出成功", response.get("message"));
    }

    @Test
    void refreshToken_success() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("validRefreshToken");

        when(jwtUtils.validateRefreshToken("validRefreshToken")).thenReturn(true);
        when(jwtUtils.extractUsername("validRefreshToken")).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtUtils.generateToken(userDetails)).thenReturn("newAccessToken");
        when(jwtUtils.generateRefreshToken(userDetails)).thenReturn("newRefreshToken");
        when(jwtUtils.getExpirationTime()).thenReturn(3600000L);

        // 假设当前没有oldToken
        when(sessionManagementService.registerUserToken("testuser", "newAccessToken", 3600L)).thenReturn(true);

        Map<String, Object> response = authService.refreshToken(request);

        assertTrue((Boolean) response.get("success"));
        assertEquals("Token刷新成功", response.get("message"));
        assertEquals("newAccessToken", response.get("accessToken"));
        assertEquals(3600L, response.get("expiresIn"));
    }

    @Test
    void kickOutUser_success() {
        doNothing().when(sessionManagementService).kickOutAllUserSessions("testuser");

        Map<String, Object> response = authService.kickOutUser("testuser");

        assertTrue((Boolean) response.get("success"));
        assertEquals("用户所有会话已被踢出", response.get("message"));
    }

    @Test
    void getUserSessions_success() {
        when(sessionManagementService.getUserActiveSessionCount("testuser")).thenReturn(2);

        Map<String, Object> response = authService.getUserSessions("testuser");

        assertTrue((Boolean) response.get("success"));
        assertEquals(2, response.get("activeSessionCount"));
    }
}