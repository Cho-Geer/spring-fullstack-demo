package com.demo.mysql_jpa_01.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT认证入口点
 * 当未认证用户访问需要认证的资源时的处理
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        
        log.warn("未授权访问: {} - {}", request.getRequestURI(), authException.getMessage());
        
        // 设置响应头
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        // 构建错误响应
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", "访问被拒绝，请先登录");
        errorResponse.put("path", request.getRequestURI());
        
        // 根据请求类型提供不同的提示信息
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null) {
            errorResponse.put("details", "缺少Authorization头");
        } else if (!authHeader.startsWith("Bearer ")) {
            errorResponse.put("details", "Authorization头格式错误，应为: Bearer <token>");
        } else {
            errorResponse.put("details", "Token无效或已过期");
        }
        
        // 写入响应
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        response.getWriter().flush();
    }
}