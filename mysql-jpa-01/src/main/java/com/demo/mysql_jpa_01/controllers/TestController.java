package com.demo.mysql_jpa_01.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.demo.mysql_jpa_01.services.TestService;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 测试控制器
 * 用于验证JWT认证和权限控制功能
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    /**
     * 公开接口 - 不需要认证
     */
    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> publicEndpoint() {
        Map<String, Object> serviceResponse = testService.getPublicMessage();
        serviceResponse.put("timestamp", LocalDateTime.now());
        serviceResponse.put("authenticated", false);
        return ResponseEntity.ok(serviceResponse);
    }

    /**
     * 需要认证的接口
     */
    @GetMapping("/authenticated")
    public ResponseEntity<Map<String, Object>> authenticatedEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> serviceResponse = testService.getProtectedMessage();
        serviceResponse.put("timestamp", LocalDateTime.now());
        serviceResponse.put("authenticated", true);
        serviceResponse.put("username", authentication.getName());
        serviceResponse.put("authorities", authentication.getAuthorities());
        
        return ResponseEntity.ok(serviceResponse);
    }

    /**
     * 需要ADMIN角色的接口
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> adminEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> serviceResponse = testService.getAdminMessage();
        serviceResponse.put("timestamp", LocalDateTime.now());
        serviceResponse.put("username", authentication.getName());
        serviceResponse.put("role", "ADMIN");
        
        return ResponseEntity.ok(serviceResponse);
    }

    /**
     * 需要USER_MANAGE权限的接口
     */
    @GetMapping("/user-manage")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<Map<String, Object>> userManageEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> serviceResponse = testService.getUserManageMessage();
        serviceResponse.put("timestamp", LocalDateTime.now());
        serviceResponse.put("username", authentication.getName());
        
        return ResponseEntity.ok(serviceResponse);
    }

    /**
     * 需要ROLE_MANAGE权限的接口
     */
    @GetMapping("/role-manage")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<Map<String, Object>> roleManageEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> serviceResponse = testService.getRoleManageMessage();
        serviceResponse.put("timestamp", LocalDateTime.now());
        serviceResponse.put("username", authentication.getName());
        
        return ResponseEntity.ok(serviceResponse);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> serviceResponse = testService.getCurrentUser();
        serviceResponse.put("principal", authentication.getPrincipal().getClass().getSimpleName());
        serviceResponse.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(serviceResponse);
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> serviceResponse = testService.healthCheck();
        serviceResponse.put("timestamp", LocalDateTime.now());
        serviceResponse.put("service", "JWT Authentication Service");
        
        return ResponseEntity.ok(serviceResponse);
    }
}