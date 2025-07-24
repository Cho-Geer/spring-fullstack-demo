package com.demo.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * JPA 审计配置
 * 启用 Spring Data JPA 的审计功能，自动填充 @CreatedBy 和 @LastModifiedBy 字段
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditConfig {

    /**
     * 提供当前审计用户信息
     * 在用户未登录时返回 "SYSTEM"，登录后返回用户名
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAware<String>() {
            @Override
            public Optional<String> getCurrentAuditor() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                
                if (authentication == null || !authentication.isAuthenticated() 
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                    // 未登录或匿名用户时使用系统标识
                    return Optional.of("SYSTEM");
                }
                
                // 已登录用户使用用户名
                return Optional.of(authentication.getName());
            }
        };
    }
}
