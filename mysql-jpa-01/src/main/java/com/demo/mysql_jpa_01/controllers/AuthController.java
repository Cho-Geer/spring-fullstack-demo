package com.demo.mysql_jpa_01.controllers;

import com.demo.mysql_jpa_01.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 认证控制器
 * 处理用户登录、登出和token刷新等认证相关操作
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证 API", description = "用户认证相关操作")
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码进行登录，返回访问令牌和刷新令牌")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest) {
        Map<String, Object> serviceResponse = authService.login(loginRequest);
        if (!(Boolean) serviceResponse.get("success")) {
            String errorCode = (String) serviceResponse.get("errorCode");
            if ("MAX_SESSIONS_EXCEEDED".equals(errorCode)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(serviceResponse);
            } else if ("INVALID_CREDENTIALS".equals(errorCode)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(serviceResponse);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(serviceResponse);
            }
        }
        return ResponseEntity.ok(serviceResponse);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "登出当前会话，移除令牌")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        Map<String, Object> serviceResponse = authService.logout(request);
        if (!(Boolean) serviceResponse.get("success")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(serviceResponse);
        }
        return ResponseEntity.ok(serviceResponse);
    }

    /**
     * 刷新token
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody RefreshTokenRequest refreshRequest) {
        Map<String, Object> serviceResponse = authService.refreshToken(refreshRequest);
        if (!(Boolean) serviceResponse.get("success")) {
            String errorCode = (String) serviceResponse.get("errorCode");
            if ("INVALID_REFRESH_TOKEN".equals(errorCode)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(serviceResponse);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(serviceResponse);
            }
        }
        return ResponseEntity.ok(serviceResponse);
    }

    /**
     * 踢出用户的所有会话
     */
    @PostMapping("/kick-out/{username}")
    @Operation(summary = "踢出用户", description = "踢出指定用户的所有会话")
    public ResponseEntity<Map<String, Object>> kickOutUser(@PathVariable String username) {
        Map<String, Object> serviceResponse = authService.kickOutUser(username);
        if (!(Boolean) serviceResponse.get("success")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(serviceResponse);
        }
        return ResponseEntity.ok(serviceResponse);
    }

    /**
     * 获取用户当前活跃会话信息
     */
    @GetMapping("/sessions/{username}")
    @Operation(summary = "获取用户会话", description = "获取指定用户的活跃会话信息")
    public ResponseEntity<Map<String, Object>> getUserSessions(@PathVariable String username) {
        Map<String, Object> serviceResponse = authService.getUserSessions(username);
        if (!(Boolean) serviceResponse.get("success")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(serviceResponse);
        }
        return ResponseEntity.ok(serviceResponse);
    }

    /**
     * 从请求中提取JWT token
     */
    

    /**
     * 登录请求DTO
     */
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * 刷新token请求DTO
     */
    public static class RefreshTokenRequest {
        private String refreshToken;

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
}