package com.demo.backend.services;  // 包声明

import lombok.RequiredArgsConstructor;  // 导入Lombok注解
import org.springframework.security.core.Authentication;  // 导入认证接口
import org.springframework.security.core.context.SecurityContextHolder;  // 导入安全上下文持有者
import org.springframework.stereotype.Service;  // 导入服务注解

import java.util.HashMap;  // 导入哈希映射类
import java.util.Map;  // 导入映射接口

/**
 * 测试服务类
 * 封装测试相关的业务逻辑
 */
@Service  // 标记为 Spring 服务组件
@RequiredArgsConstructor  // 生成带参构造函数
public class TestService {  // 类定义

    /**
     * 获取公开消息
     * @return 响应映射
     */
    public Map<String, Object> getPublicMessage() {  // 获取公开消息方法定义
        Map<String, Object> response = new HashMap<>();  // 创建响应映射
        response.put("message", "这是一个公开接口，无需认证即可访问。");  // 设置消息
        return response;  // 返回响应
    }  // 结束获取公开消息方法

    /**
     * 获取受保护消息
     * @return 响应映射
     */
    public Map<String, Object> getProtectedMessage() {  // 获取受保护消息方法定义
        Map<String, Object> response = new HashMap<>();  // 创建响应映射
        response.put("message", "这是一个受保护的接口，需要认证后才能访问。");  // 设置消息
        return response;  // 返回响应
    }  // 结束获取受保护消息方法

    /**
     * 获取管理员消息
     * @return 响应映射
     */
    public Map<String, Object> getAdminMessage() {  // 获取管理员消息方法定义
        Map<String, Object> response = new HashMap<>();  // 创建响应映射
        response.put("message", "这是一个管理员接口，需要 ADMIN 角色才能访问。");  // 设置消息
        return response;  // 返回响应
    }  // 结束获取管理员消息方法

    /**
     * 获取当前用户信息
     * @return 响应映射
     */
    public Map<String, Object> getCurrentUser() {  // 获取当前用户信息方法定义
        Map<String, Object> response = new HashMap<>();  // 创建响应映射
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();  // 获取认证信息
        if (authentication != null && authentication.isAuthenticated()) {  // 如果已认证
            response.put("username", authentication.getName());  // 添加用户名
            response.put("authorities", authentication.getAuthorities());  // 添加权限
        } else {  // 否则
            response.put("message", "用户未认证");  // 设置消息
        }  // 结束else块
        return response;  // 返回响应
    }  // 结束获取当前用户信息方法

    /**
     * 健康检查
     * @return 响应映射
     */
    public Map<String, Object> healthCheck() {  // 健康检查方法定义
        Map<String, Object> response = new HashMap<>();  // 创建响应映射
        response.put("status", "UP");  // 设置状态
        response.put("message", "应用运行正常");  // 设置消息
        return response;  // 返回响应
    }  // 结束健康检查方法

    /**
     * 获取用户管理消息
     * @return 响应映射
     */
    public Map<String, Object> getUserManageMessage() {  // 获取用户管理消息方法定义
        Map<String, Object> response = new HashMap<>();  // 创建响应映射
        response.put("message", "这是一个需要USER_MANAGE权限的接口");  // 设置消息
        response.put("permission", "USER_MANAGE");  // 设置权限
        return response;  // 返回响应
    }  // 结束获取用户管理消息方法

    /**
     * 获取角色管理消息
     * @return 响应映射
     */
    public Map<String, Object> getRoleManageMessage() {  // 获取角色管理消息方法定义
        Map<String, Object> response = new HashMap<>();  // 创建响应映射
        response.put("message", "这是一个需要ROLE_MANAGE权限的接口");  // 设置消息
        response.put("permission", "ROLE_MANAGE");  // 设置权限
        return response;  // 返回响应
    }  // 结束获取角色管理消息方法
}  // 结束类