package com.demo.backend.config;

import com.demo.backend.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * JWT密钥配置类
 * 确保JWT_SECRET环境变量在启动时已设置
 */
@Configuration
@Slf4j
public class JwtSecretConfig {

    private final JwtUtils jwtUtils;

    public JwtSecretConfig(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    /**
     * 启动时检查JWT_SECRET是否设置
     * 如果未设置，应用将启动失败
     */
    @PostConstruct
    public void validateJwtSecret() {
        // JwtUtils已经从配置文件中读取了secret值
        // 这里我们通过反射获取实际使用的secret值进行验证
        try {
            java.lang.reflect.Field secretField = JwtUtils.class.getDeclaredField("secret");
            secretField.setAccessible(true);
            String jwtSecret = (String) secretField.get(jwtUtils);
            
            if (jwtSecret == null || jwtSecret.isEmpty()) {
                String errorMessage = "JWT_SECRET environment variable is not set. Please set it before starting the application.";
                log.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
            
            if (jwtSecret.length() < 32) {
                String errorMessage = "JWT_SECRET must be at least 32 bytes long. Current length: " + jwtSecret.length();
                log.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
            
            log.info("JWT_SECRET validation passed");
        } catch (Exception e) {
            log.error("Failed to validate JWT_SECRET: {}", e.getMessage());
            throw new RuntimeException("Failed to validate JWT_SECRET", e);
        }
    }
}
