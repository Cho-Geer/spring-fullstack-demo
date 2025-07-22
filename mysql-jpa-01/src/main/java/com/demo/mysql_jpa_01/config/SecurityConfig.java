package com.demo.mysql_jpa_01.config;

import com.demo.mysql_jpa_01.security.JwtAccessDeniedHandler;
import com.demo.mysql_jpa_01.security.JwtAuthenticationEntryPoint;
import com.demo.mysql_jpa_01.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security配置类
 * 配置JWT认证、权限控制和安全策略
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // 启用方法级安全注解
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    /**
     * 安全过滤器链配置
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（JWT无状态认证不需要）
            .csrf(AbstractHttpConfigurer::disable)
            
            // 启用CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 配置会话管理为无状态（JWT认证）
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 配置认证提供者
            .authenticationProvider(authenticationProvider())
            
            // 添加JWT认证过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // 配置异常处理
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
            )
            
            // 配置授权规则
            .authorizeHttpRequests(auth -> auth
                // 公开端点 - 不需要认证
                .requestMatchers(
                    "/api/auth/**",           // 认证相关接口
                    "/api/public/**",        // 公开API
                    "/api/test/public",      // 测试公开接口
                    "/api/test/health",      // 测试健康检查
                    "/",                     // 首页
                    "/login",                // 登录页面
                    "/register",             // 注册页面
                    "/css/**",               // 静态资源
                    "/js/**",
                    "/images/**",
                    "/favicon.ico",
                    "/actuator/health"       // 健康检查
                ).permitAll()
                
                // 管理员接口 - 需要ADMIN角色
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // 用户管理接口 - 需要USER_MANAGE权限
                .requestMatchers("/api/users/**").hasAuthority("USER_MANAGE")
                
                // 角色管理接口 - 需要ROLE_MANAGE权限
                .requestMatchers("/api/roles/**").hasAuthority("ROLE_MANAGE")
                
                // 权限管理接口 - 需要PERMISSION_MANAGE权限
                .requestMatchers("/api/permissions/**").hasAuthority("PERMISSION_MANAGE")
                
                // 其他所有请求都需要认证
                .anyRequest().authenticated()
            );
        
        return http.build();
    }

    /**
     * 安全配置说明：
     * 1. JWT认证入口点：处理未认证用户的访问
     * 2. JWT访问拒绝处理器：处理权限不足的访问
     * 3. JWT认证过滤器：验证每个请求的JWT token
     * 4. 无状态会话：不使用HTTP会话，完全依赖JWT
     * 5. CORS支持：允许跨域请求
     * 6. 方法级安全：支持@PreAuthorize等注解
     */

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证提供者
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * CORS配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许的源
        configuration.setAllowedOriginPatterns(List.of("*"));
        
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // 允许的请求头
        configuration.setAllowedHeaders(List.of("*"));
        
        // 允许发送凭证
        configuration.setAllowCredentials(true);
        
        // 预检请求的缓存时间
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}