package com.demo.backend.services;  // 包声明

import com.demo.backend.controllers.AuthController.LoginRequest;  // 导入登录请求类
import com.demo.backend.controllers.AuthController.RefreshTokenRequest;  // 导入刷新令牌请求类
import com.demo.backend.controllers.AuthController.RegisterRequest;  // 导入注册请求类
import com.demo.backend.entities.UserEntity;  // 导入用户实体类
import com.demo.backend.entities.RoleEntity;  // 导入角色实体类
import com.demo.backend.repositories.UserRepository;  // 导入用户仓库
import com.demo.backend.repositories.RoleRepository;  // 导入角色仓库
import com.demo.backend.utils.JwtUtils;  // 导入JWT工具类
import jakarta.servlet.http.HttpServletRequest;  // 导入HTTP请求类
import lombok.RequiredArgsConstructor;  // 导入Lombok注解
import lombok.extern.slf4j.Slf4j;  // 导入日志注解
import org.springframework.security.authentication.AuthenticationManager;  // 导入认证管理器
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;  // 导入用户名密码认证令牌
import org.springframework.security.core.Authentication;  // 导入认证接口
import org.springframework.security.core.AuthenticationException;  // 导入认证异常
import org.springframework.security.core.context.SecurityContextHolder;  // 导入安全上下文持有者
import org.springframework.security.core.userdetails.UserDetails;  // 导入用户详情接口
import org.springframework.security.crypto.password.PasswordEncoder;  // 导入密码编码器
import org.springframework.stereotype.Service;  // 导入服务注解
import org.springframework.transaction.annotation.Transactional;  // 导入事务注解
import org.springframework.util.StringUtils;  // 导入字符串工具类

import java.time.LocalDateTime;  // 导入本地日期时间类
import java.util.HashMap;  // 导入哈希映射类
import java.util.Map;  // 导入映射接口

/**
 * 认证服务类
 * 封装用户认证相关的业务逻辑
 */
@Slf4j  // 启用日志记录
@Service  // 标记为 Spring 服务组件
@RequiredArgsConstructor  // 生成带参构造函数
public class AuthService {  // 类定义

    private final AuthenticationManager authenticationManager;  // 认证管理器字段
    private final JwtUtils jwtUtils;  // JWT 工具类字段
    private final SessionManagementService sessionManagementService;  // 会话管理服务字段
    private final UserRepository userRepository;  // 用户仓库字段
    private final RoleRepository roleRepository;  // 角色仓库字段
    private final PasswordEncoder passwordEncoder;  // 密码编码器字段

    /**
     * 处理用户登录逻辑
     * @param loginRequest 登录请求
     * @return 响应映射
     */
    public Map<String, Object> login(LoginRequest loginRequest) {  // 登录方法定义
        Map<String, Object> response = new HashMap<>();  // 创建响应映射
        try {  // 开始try块
            Authentication authentication = authenticationManager.authenticate(  // 进行身份验证
                    new UsernamePasswordAuthenticationToken(  // 创建认证令牌
                            loginRequest.getUsername(),  // 获取用户名
                            loginRequest.getPassword()  // 获取密码
                    )
            );  // 结束认证调用
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();  // 获取用户详情
            String username = userDetails.getUsername();  // 获取用户名
            String accessToken = jwtUtils.generateToken(userDetails);  // 生成访问令牌
            String refreshToken = jwtUtils.generateRefreshToken(userDetails);  // 生成刷新令牌
            long expirationSeconds = jwtUtils.getExpirationTime() / 1000;  // 计算过期时间（秒）
            boolean registered = sessionManagementService.registerUserToken(username, accessToken, expirationSeconds);  // 注册用户令牌
            if (!registered) {  // 如果注册失败
                response.put("success", false);  // 设置成功标志为 false
                response.put("message", "登录失败：已达到最大并发会话数限制");  // 设置错误消息
                response.put("errorCode", "MAX_SESSIONS_EXCEEDED");  // 设置错误代码
                return response;  // 返回响应
            }  // 结束if块
            response.put("success", true);  // 设置成功标志为 true
            response.put("message", "登录成功");  // 设置成功消息
            response.put("accessToken", accessToken);  // 添加访问令牌
            response.put("refreshToken", refreshToken);  // 添加刷新令牌
            response.put("tokenType", "Bearer");  // 设置令牌类型
            response.put("expiresIn", expirationSeconds);  // 添加过期时间
            response.put("username", username);  // 添加用户名
            response.put("authorities", userDetails.getAuthorities());  // 添加权限
            response.put("loginTime", LocalDateTime.now());  // 添加登录时间
            int activeSessionCount = sessionManagementService.getUserActiveSessionCount(username);  // 获取活跃会话数
            response.put("activeSessionCount", activeSessionCount);  // 添加活跃会话数
            log.info("用户 {} 登录成功，当前活跃会话数: {}", username, activeSessionCount);  // 记录日志
            return response;  // 返回响应
        } catch (AuthenticationException e) {  // 捕获认证异常
            log.warn("用户 {} 登录失败: {}", loginRequest.getUsername(), e.getMessage());  // 记录警告日志
            response.put("success", false);  // 设置成功标志为 false
            response.put("message", "登录失败：用户名或密码错误");  // 设置错误消息
            response.put("errorCode", "INVALID_CREDENTIALS");  // 设置错误代码
            response.put("timestamp", LocalDateTime.now());  // 添加时间戳
            return response;  // 返回响应
        } catch (Exception e) {  // 捕获其他异常
            log.error("登录过程中发生错误: {}", e.getMessage(), e);  // 记录错误日志
            response.put("success", false);  // 设置成功标志为 false
            response.put("message", "登录失败：系统错误");  // 设置错误消息
            response.put("errorCode", "SYSTEM_ERROR");  // 设置错误代码
            response.put("timestamp", LocalDateTime.now());  // 添加时间戳
            return response;  // 返回响应
        }  // 结束catch块
    }  // 结束登录方法

    /**
     * 处理用户注册逻辑
     * @param registerRequest 注册请求
     * @return 响应映射
     */
    @Transactional  // 事务注解
    public Map<String, Object> register(RegisterRequest registerRequest) {  // 注册方法定义
        Map<String, Object> response = new HashMap<>();  // 创建响应映射
        try {  // 开始try块
            // 检查用户名是否已存在
            if (userRepository.existsByUsername(registerRequest.getUsername())) {  // 检查用户名是否存在
                response.put("success", false);  // 设置成功标志为 false
                response.put("message", "注册失败：用户名已存在");  // 设置错误消息
                response.put("errorCode", "USERNAME_EXISTS");  // 设置错误代码
                return response;  // 返回响应
            }  // 结束if块
            
            // 检查邮箱是否已存在
            if (userRepository.existsByEmail(registerRequest.getEmail())) {  // 检查邮箱是否存在
                response.put("success", false);  // 设置成功标志为 false
                response.put("message", "注册失败：邮箱已存在");  // 设置错误消息
                response.put("errorCode", "EMAIL_EXISTS");  // 设置错误代码
                return response;  // 返回响应
            }  // 结束if块
            
            // 获取默认角色
            RoleEntity defaultRole = roleRepository.findByName("ROLE_USER")  // 查找默认角色
                    .orElseThrow(() -> new RuntimeException("默认角色不存在"));  // 抛出异常如果角色不存在
            
            // 创建新用户
            UserEntity newUser = new UserEntity();  // 创建用户实体
            newUser.setUsername(registerRequest.getUsername());  // 设置用户名
            newUser.setEmail(registerRequest.getEmail());  // 设置邮箱
            newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));  // 编码并设置密码
            newUser.setEnabled(true);  // 设置启用状态
            newUser.getRoles().add(defaultRole);  // 添加默认角色
            
            // 保存用户
            UserEntity savedUser = userRepository.save(newUser);  // 保存用户
            
            response.put("success", true);  // 设置成功标志为 true
            response.put("message", "注册成功");  // 设置成功消息
            response.put("username", savedUser.getUsername());  // 添加用户名
            response.put("email", savedUser.getEmail());  // 添加邮箱
            response.put("timestamp", LocalDateTime.now());  // 添加时间戳
            
            log.info("用户 {} 注册成功", savedUser.getUsername());  // 记录日志
            return response;  // 返回响应
            
        } catch (Exception e) {  // 捕获异常
            log.error("注册过程中发生错误: {}", e.getMessage(), e);  // 记录错误日志
            response.put("success", false);  // 设置成功标志为 false
            response.put("message", "注册失败：系统错误");  // 设置错误消息
            response.put("errorCode", "SYSTEM_ERROR");  // 设置错误代码
            response.put("timestamp", LocalDateTime.now());  // 添加时间戳
            return response;  // 返回响应
        }  // 结束catch块
    }  // 结束注册方法

    /**
     * 处理用户登出逻辑
     * @param request HTTP请求
     * @return 响应映射
     */
    public Map<String, Object> logout(HttpServletRequest request) {  // 登出方法定义
        Map<String, Object> response = new HashMap<>();  // 创建响应映射
        try {  // 开始try块
            String token = extractTokenFromRequest(request);  // 提取令牌
            if (token != null) {  // 如果令牌存在
                sessionManagementService.removeUserToken(token);  // 移除用户令牌
                SecurityContextHolder.clearContext();  // 清除安全上下文
                log.info("用户登出成功，token已失效");  // 记录日志
            }  // 结束if块
            response.put("success", true);  // 设置成功标志为 true
            response.put("message", "登出成功");  // 设置成功消息
            response.put("timestamp", LocalDateTime.now());  // 添加时间戳
            return response;  // 返回响应
        } catch (Exception e) {  // 捕获异常
            log.error("登出过程中发生错误: {}", e.getMessage(), e);  // 记录错误日志
            response.put("success", false);  // 设置成功标志为 false
            response.put("message", "登出失败：系统错误");  // 设置错误消息
            response.put("errorCode", "SYSTEM_ERROR");  // 设置错误代码
            response.put("timestamp", LocalDateTime.now());  // 添加时间戳
            return response;  // 返回响应
        }  // 结束catch块
    }  // 结束登出方法

    /**
     * 处理token刷新逻辑
     * @param refreshRequest 刷新请求
     * @return 响应映射
     */
    public Map<String, Object> refreshToken(RefreshTokenRequest refreshRequest) {  // 刷新令牌方法定义
        Map<String, Object> response = new HashMap<>();  // 创建响应映射
        try {  // 开始try块
            String refreshToken = refreshRequest.getRefreshToken();  // 获取刷新令牌
            if (!jwtUtils.validateRefreshToken(refreshToken)) {  // 验证刷新令牌
                response.put("success", false);  // 设置成功标志为 false
                response.put("message", "刷新token无效或已过期");  // 设置错误消息
                response.put("errorCode", "INVALID_REFRESH_TOKEN");  // 设置错误代码
                return response;  // 返回响应
            }  // 结束if块
            String username = jwtUtils.extractUsername(refreshToken);  // 提取用户名
            String newAccessToken = jwtUtils.generateTokenFromUsername(username);  // 生成新访问令牌
            long expirationSeconds = jwtUtils.getExpirationTime() / 1000;  // 计算过期时间
            String currentToken = extractTokenFromRequest(null);  // 提取当前令牌（可能需调整）
            if (currentToken != null) {  // 如果当前令牌存在
                sessionManagementService.refreshUserToken(currentToken, newAccessToken, expirationSeconds);  // 刷新用户令牌
            } else {  // 否则
                sessionManagementService.registerUserToken(username, newAccessToken, expirationSeconds);  // 注册用户令牌
            }  // 结束else块
            response.put("success", true);  // 设置成功标志为 true
            response.put("message", "Token刷新成功");  // 设置成功消息
            response.put("accessToken", newAccessToken);  // 添加新访问令牌
            response.put("tokenType", "Bearer");  // 设置令牌类型
            response.put("expiresIn", expirationSeconds);  // 添加过期时间
            response.put("timestamp", LocalDateTime.now());  // 添加时间戳
            log.info("用户 {} 的token刷新成功", username);  // 记录日志
            return response;  // 返回响应
        } catch (Exception e) {  // 捕获异常
            log.error("刷新token过程中发生错误: {}", e.getMessage(), e);  // 记录错误日志
            response.put("success", false);  // 设置成功标志为 false
            response.put("message", "刷新token失败：系统错误");  // 设置错误消息
            response.put("errorCode", "SYSTEM_ERROR");  // 设置错误代码
            response.put("timestamp", LocalDateTime.now());  // 添加时间戳
            return response;  // 返回响应
        }  // 结束catch块
    }  // 结束刷新令牌方法

    /**
     * 踢出用户的所有会话
     * @param username 用户名
     * @return 响应映射
     */
    public Map<String, Object> kickOutUser(String username) {  // 踢出用户方法定义
        Map<String, Object> response = new HashMap<>();  // 创建响应映射
        try {  // 开始try块
            sessionManagementService.kickOutAllUserSessions(username);  // 踢出所有用户会话
            response.put("success", true);  // 设置成功标志为 true
            response.put("message", "用户所有会话已被踢出");  // 设置成功消息
            response.put("username", username);  // 添加用户名
            response.put("timestamp", LocalDateTime.now());  // 添加时间戳
            log.info("管理员踢出用户 {} 的所有会话", username);  // 记录日志
            return response;  // 返回响应
        } catch (Exception e) {  // 捕获异常
            log.error("踢出用户会话过程中发生错误: {}", e.getMessage(), e);  // 记录错误日志
            response.put("success", false);  // 设置成功标志为 false
            response.put("message", "踢出用户会话失败：系统错误");  // 设置错误消息
            response.put("errorCode", "SYSTEM_ERROR");  // 设置错误代码
            response.put("timestamp", LocalDateTime.now());  // 添加时间戳
            return response;  // 返回响应
        }  // 结束catch块
    }  // 结束踢出用户方法

    /**
     * 获取用户当前活跃会话信息
     * @param username 用户名
     * @return 响应映射
     */
    public Map<String, Object> getUserSessions(String username) {  // 获取用户会话方法定义
        Map<String, Object> response = new HashMap<>();  // 创建响应映射
        try {  // 开始try块
            int activeSessionCount = sessionManagementService.getUserActiveSessionCount(username);  // 获取活跃会话数
            response.put("success", true);  // 设置成功标志为 true
            response.put("username", username);  // 添加用户名
            response.put("activeSessionCount", activeSessionCount);  // 添加活跃会话数
            response.put("timestamp", LocalDateTime.now());  // 添加时间戳
            return response;  // 返回响应
        } catch (Exception e) {  // 捕获异常
            log.error("获取用户会话信息过程中发生错误: {}", e.getMessage(), e);  // 记录错误日志
            response.put("success", false);  // 设置成功标志为 false
            response.put("message", "获取会话信息失败：系统错误");  // 设置错误消息
            response.put("errorCode", "SYSTEM_ERROR");  // 设置错误代码
            response.put("timestamp", LocalDateTime.now());  // 添加时间戳
            return response;  // 返回响应
        }  // 结束catch块
    }  // 结束获取用户会话方法

    /**
     * 从请求中提取JWT token
     * @param request HTTP请求
     * @return token字符串或null
     */
    private String extractTokenFromRequest(HttpServletRequest request) {  // 提取令牌方法定义
        if (request == null) {  // 如果请求为空
            return null;  // 返回 null
        }  // 结束if块
        String bearerToken = request.getHeader("Authorization");  // 获取 Authorization 头
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {  // 如果头有效且以 Bearer 开头
            return bearerToken.substring(7);  // 提取令牌
        }  // 结束if块
        return null;  // 返回 null
    }  // 结束提取令牌方法
}  // 结束类