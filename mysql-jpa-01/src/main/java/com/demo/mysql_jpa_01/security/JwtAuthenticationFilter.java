package com.demo.mysql_jpa_01.security;

import com.demo.mysql_jpa_01.services.SessionManagementService;
import com.demo.mysql_jpa_01.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 * 拦截每个请求，验证JWT token并设置Spring Security上下文
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final SessionManagementService sessionManagementService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();
        log.debug("JwtAuthenticationFilter.doFilterInternal开始处理请求: {}", path);
        
        // 记录Authorization头信息（脱敏处理）
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        log.debug("Authorization头: {}", authHeader != null ? 
                (authHeader.startsWith(BEARER_PREFIX) ? BEARER_PREFIX + "***" : "非Bearer格式") : "null");
        
        try {
            // 从请求头中提取JWT token
            String token = extractTokenFromRequest(request);
            log.debug("提取到token: {}", token != null ? "是" : "否");
            
            // 如果token存在且当前没有认证信息
            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 从token中提取用户名
                String username = jwtUtils.extractUsername(token);
                log.debug("从token中提取的用户名: {}", username != null ? username : "null");
                
                if (username != null) {
                    // 加载用户详情
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    log.debug("加载到用户详情: {}", userDetails != null ? "是" : "否");
                    
                    // 验证token是否有效（JWT格式验证 + Redis会话验证）
                    boolean jwtValid = jwtUtils.validateToken(token, userDetails);
                    boolean sessionValid = sessionManagementService.isTokenValid(token);
                    log.debug("JWT验证结果: {}, 会话验证结果: {}", jwtValid, sessionValid);
                    
                    if (jwtValid && sessionValid) {
                        // 创建认证对象
                        UsernamePasswordAuthenticationToken authToken = 
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );
                        
                        // 设置认证详情
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // 设置到Spring Security上下文
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        
                        log.debug("用户 {} 通过JWT认证成功, 权限: {}", username, userDetails.getAuthorities());
                    } else {
                        log.warn("用户 {} 的JWT token验证失败或会话已失效, JWT验证: {}, 会话验证: {}", 
                                username, jwtValid, sessionValid);
                    }
                }
            }
        } catch (Exception e) {
            log.error("JWT认证过程中发生错误: {}", e.getMessage(), e);
            // 清除可能存在的认证信息
            SecurityContextHolder.clearContext();
        }
        
        // 继续过滤器链
        log.debug("JwtAuthenticationFilter处理完成，继续过滤器链, 路径: {}", path);
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中提取JWT token
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        // 从Authorization头中提取
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        
        // 也可以从请求参数中提取（可选）
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam;
        }
        
        return null;
    }

    /**
     * 判断是否应该跳过此过滤器
     * 对于某些路径（如登录、注册）可以跳过JWT验证
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        
        // 如果有上下文路径，需要去除上下文路径再判断
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        
        // 详细记录请求路径
        log.debug("JwtAuthenticationFilter.shouldNotFilter检查路径: {}, 上下文路径: {}", path, contextPath);
        
        // 检查是否应该跳过过滤器
        boolean shouldSkip = path.startsWith("/auth/") ||
               path.startsWith("/public/") ||
               path.equals("/") ||
               path.isEmpty() || // 处理去除上下文路径后为空的情况
               path.startsWith("/static/") ||
               path.startsWith("/css/") ||
               path.startsWith("/js/") ||
               path.startsWith("/images/") ||
               path.startsWith("/favicon.ico") ||
               path.startsWith("/actuator/health") ||
               path.equals("/error") ||
               path.equals("/api/error");
        
        // 记录判断结果
        log.debug("JwtAuthenticationFilter对路径[{}]的判断结果: {}", path, shouldSkip ? "跳过过滤器" : "执行过滤器");
        
        return shouldSkip;
    }
}