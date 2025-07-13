package com.demo.mysql_jpa_01.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // Spring Securityの設定を有効化
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF保護（デフォルト有効、APIの場合は無効にすることも）
            .csrf(csrf -> csrf.
                disable()
            ) 
            
            // セッション管理（デフォルトはSTATELESSではない）
            .sessionManagement(session -> session
                .maximumSessions(1) // 并发会话控制​：限制同一用户的最大登录数（如 sessionManagement().maximumSessions(1)）
            )
            
            // 認可ルール（パスごとのアクセス制御）
            .authorizeHttpRequests(auth -> auth
                // 静的リソース（CSS/JS/画像）やAPIを公開
                .requestMatchers("/css/**", "/js/**", "/images/**", "/api/public/**").permitAll()
                // ログインページは公開
                .requestMatchers("/login", "/register").permitAll()
                // 管理者向けパスはROLE_ADMINが必要
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // その他すべてのパスは認証必須
                .anyRequest().authenticated()
            )
            
            // フォームベースのログイン設定
            .formLogin(form -> form
                .loginPage("/login") // カスタムログインページ（省略するとデフォルト）
                .defaultSuccessUrl("/", true) // ログイン成功後のリダイレクト先
                .permitAll() // 誰でもログインページにアクセス可能
            )
            
            // ログアウト設定
            .logout(logout -> logout
                .logoutSuccessUrl("/") // ログアウト後のリダイレクト先
                .invalidateHttpSession(true) // セッションを破棄
                .deleteCookies("JSESSIONID") // クッキーを削除
                .permitAll()
            );
        
        return http.build();
    }
}