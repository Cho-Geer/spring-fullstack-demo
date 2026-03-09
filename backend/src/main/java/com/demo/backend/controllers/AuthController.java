package com.demo.backend.controllers;

import com.demo.backend.services.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 认证控制器
 * 处理用户登录、登出和token刷新等认证相关操作
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证 API", description = "用户认证相关操作")
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "使用用户名、邮箱和密码进行注册")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        Map<String, Object> serviceResponse = authService.register(registerRequest);
        if (!(Boolean) serviceResponse.get("success")) {
            String errorCode = (String) serviceResponse.get("errorCode");
            if ("USERNAME_EXISTS".equals(errorCode) || "EMAIL_EXISTS".equals(errorCode)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(serviceResponse);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(serviceResponse);
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceResponse);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码进行登录，返回访问令牌，刷新令牌存储在HttpOnly Cookie中")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
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
        
        // 设置HttpOnly Cookie存储refreshToken
        String refreshToken = (String) serviceResponse.get("refreshToken");
        if (refreshToken != null) {
            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(false); // 开发环境使用false，生产环境应改为true
            refreshTokenCookie.setSameSite(Cookie.SameSite.STRICT);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7天过期
            response.addCookie(refreshTokenCookie);
            
            // 从响应中移除refreshToken，避免在响应体中返回
            serviceResponse.remove("refreshToken");
        }
        
        return ResponseEntity.ok(serviceResponse);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "登出当前会话，移除令牌和刷新令牌Cookie")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> serviceResponse = authService.logout(request);
        
        // 清除refreshToken Cookie
        clearRefreshTokenCookie(response);
        
        if (!(Boolean) serviceResponse.get("success")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(serviceResponse);
        }
        return ResponseEntity.ok(serviceResponse);
    }

    /**
     * 刷新token
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "从HttpOnly Cookie中读取刷新令牌获取新的访问令牌")
    public ResponseEntity<Map<String, Object>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // 从Cookie中获取refreshToken
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        
        if (refreshToken == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "刷新令牌不存在");
            errorResponse.put("errorCode", "INVALID_REFRESH_TOKEN");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        // 创建刷新请求对象
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(refreshToken);
        
        Map<String, Object> serviceResponse = authService.refreshToken(refreshRequest);
        if (!(Boolean) serviceResponse.get("success")) {
            String errorCode = (String) serviceResponse.get("errorCode");
            if ("INVALID_REFRESH_TOKEN".equals(errorCode)) {
                // 清除无效的refreshToken cookie
                clearRefreshTokenCookie(response);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(serviceResponse);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(serviceResponse);
            }
        }
        
        // 生成新的refreshToken并更新Cookie
        String newRefreshToken = (String) serviceResponse.get("refreshToken");
        if (newRefreshToken != null) {
            Cookie refreshTokenCookie = new Cookie("refreshToken", newRefreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(false); // 开发环境使用false，生产环境应改为true
            refreshTokenCookie.setSameSite(Cookie.SameSite.STRICT);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7天过期
            response.addCookie(refreshTokenCookie);
            
            // 从响应中移除refreshToken，避免在响应体中返回
            serviceResponse.remove("refreshToken");
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
     * 清除刷新令牌Cookie
     */
    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", "");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);
        refreshTokenCookie.setSameSite(Cookie.SameSite.STRICT);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0); // 设置为0立即过期
        response.addCookie(refreshTokenCookie);
    }

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
     * 注册请求DTO
     */
    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
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