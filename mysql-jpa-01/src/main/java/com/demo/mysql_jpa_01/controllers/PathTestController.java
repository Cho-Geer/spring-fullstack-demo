package com.demo.mysql_jpa_01.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 路径测试控制器
 * 用于测试不同路径的认证行为
 */
@Slf4j
@RestController
public class PathTestController {

    /**
     * 测试/api/auth/路径
     */
    @GetMapping("/api/auth/test-path")
    public ResponseEntity<Map<String, Object>> testAuthPath(HttpServletRequest request) {
        return createResponse(request, "这是/api/auth/路径测试");
    }

    /**
     * 测试/api/public/路径
     */
    @GetMapping("/api/public/test-path")
    public ResponseEntity<Map<String, Object>> testPublicPath(HttpServletRequest request) {
        return createResponse(request, "这是/api/public/路径测试");
    }

    /**
     * 测试/api/protected/路径
     */
    @GetMapping("/api/protected/test-path")
    public ResponseEntity<Map<String, Object>> testProtectedPath(HttpServletRequest request) {
        return createResponse(request, "这是/api/protected/路径测试");
    }

    /**
     * 创建统一的响应
     */
    private ResponseEntity<Map<String, Object>> createResponse(HttpServletRequest request, String message) {
        Map<String, Object> response = new HashMap<>();
        
        // 获取当前认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // 记录请求信息
        log.info("收到请求: {} {}", request.getMethod(), request.getRequestURI());
        log.info("认证状态: {}", authentication != null ? "已认证" : "未认证");
        
        response.put("message", message);
        response.put("path", request.getRequestURI());
        response.put("method", request.getMethod());
        
        if (authentication != null) {
            response.put("authenticated", true);
            response.put("principal", authentication.getName());
            response.put("authorities", authentication.getAuthorities().toString());
        } else {
            response.put("authenticated", false);
        }
        
        return ResponseEntity.ok(response);
    }
}