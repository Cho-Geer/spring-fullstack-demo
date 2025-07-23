package com.demo.mysql_jpa_01.controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 全局错误控制器
 * 处理Spring Boot默认的/error端点
 */
@Slf4j
@RestController
public class CustomErrorController implements ErrorController {

    /**
     * 处理/error路径的请求
     * 这是Spring Boot的默认错误处理端点
     */
    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        // 获取错误状态码
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object requestUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        
        int statusCode = status != null ? Integer.parseInt(status.toString()) : 500;
        HttpStatus httpStatus = HttpStatus.valueOf(statusCode);
        
        log.error("错误处理 - 状态码: {}, URI: {}, 异常: {}, 消息: {}", 
                statusCode, requestUri, exception, message);
        
        // 构建错误响应
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", statusCode);
        errorResponse.put("error", httpStatus.getReasonPhrase());
        errorResponse.put("path", requestUri != null ? requestUri.toString() : request.getRequestURI());
        
        // 根据不同的错误状态码提供不同的消息
        switch (statusCode) {
            case 400:
                errorResponse.put("message", "请求参数错误");
                errorResponse.put("details", "请检查请求参数的格式和内容");
                break;
            case 401:
                errorResponse.put("message", "未授权访问");
                errorResponse.put("details", "请先登录或检查访问令牌");
                break;
            case 403:
                errorResponse.put("message", "访问被拒绝");
                errorResponse.put("details", "您没有访问此资源的权限");
                break;
            case 404:
                errorResponse.put("message", "资源未找到");
                errorResponse.put("details", "请求的资源不存在");
                break;
            case 405:
                errorResponse.put("message", "请求方法不被允许");
                errorResponse.put("details", "请检查HTTP请求方法");
                break;
            case 500:
                errorResponse.put("message", "服务器内部错误");
                errorResponse.put("details", "服务器处理请求时发生错误");
                break;
            default:
                errorResponse.put("message", message != null ? message.toString() : "未知错误");
                errorResponse.put("details", "请联系系统管理员");
        }
        
        // 添加异常信息（仅在开发环境）
        if (exception != null) {
            errorResponse.put("exception", exception.getClass().getSimpleName());
        }
        
        // 添加请求信息
        errorResponse.put("method", request.getMethod());
        errorResponse.put("userAgent", request.getHeader("User-Agent"));
        
        return ResponseEntity.status(httpStatus).body(errorResponse);
    }
    
    /**
     * 专门处理/api/error路径的请求
     */
    @RequestMapping("/api/error")
    public ResponseEntity<Map<String, Object>> apiErrorInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", new Date().toString());
        response.put("status", 200);
        response.put("message", "API Error Information Page");
        response.put("path", "/api/error");
        response.put("description", "这个端点提供了关于API错误处理系统的信息。Spring Boot应用程序使用全局异常处理器来统一处理各种错误类型，并返回标准化的JSON响应。");
        
        // 添加可用的API端点信息
        List<String> endpoints = Arrays.asList(
            "/api/test/public - 公开测试接口，无需认证",
            "/api/test/authenticated - 需要JWT认证的接口",
            "/api/test/admin - 需要管理员权限的接口",
            "/api/test/permission - 需要特定权限的接口",
            "/api/auth/register - 用户注册接口",
            "/api/auth/login - 用户登录接口",
            "/api/auth/logout - 用户登出接口",
            "/api/roles - 角色管理接口（需要管理员权限）",
            "/api/error - 错误处理信息页面",
            "/error - Spring Boot默认错误处理端点"
        );
        response.put("availableEndpoints", endpoints);
        
        // 添加错误处理说明
        List<String> errorHandling = Arrays.asList(
            "400 - 请求参数错误或数据验证失败",
            "401 - 未授权访问，需要有效的JWT令牌",
            "403 - 访问被拒绝，权限不足",
            "404 - 请求的资源未找到",
            "405 - HTTP请求方法不被允许",
            "500 - 服务器内部错误或运行时异常"
        );
        response.put("errorHandling", errorHandling);
        
        return ResponseEntity.ok(response);
    }
}