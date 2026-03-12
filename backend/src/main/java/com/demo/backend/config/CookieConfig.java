package com.demo.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.cookie")
@Getter
@Setter
public class CookieConfig {

    private boolean secure = false;
    
    private String sameSite = "Lax";
    
    public String getSameSite() {
        return sameSite;
    }
}
