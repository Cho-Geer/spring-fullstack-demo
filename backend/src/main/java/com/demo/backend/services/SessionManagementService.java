package com.demo.backend.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 会话管理服务
 * 基于Redis实现JWT token的并发控制
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionManagementService {

    private final RedisTemplate<String, Object> redisTemplate;

    // 用户活跃token的Redis key前缀
    private static final String USER_TOKENS_PREFIX = "user:tokens:";
    
    // token到用户的映射key前缀
    private static final String TOKEN_USER_PREFIX = "token:user:";
    
    // token黑名单的Redis key前缀
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    
    // 最大并发会话数
    @Value("${app.security.concurrent.max-sessions:1}")
    private int maxSessions;
    
    // 是否阻止超出最大会话数的登录
    @Value("${app.security.concurrent.prevent-login-if-maximum-exceeded:false}")
    private boolean preventLoginIfMaximumExceeded;

    /**
     * 用户登录时注册token
     * @param username 用户名
     * @param token JWT token
     * @param expirationSeconds token过期时间（秒）
     * @return 是否注册成功
     */
    public boolean registerUserToken(String username, String token, long expirationSeconds) {
        String userTokensKey = USER_TOKENS_PREFIX + username;
        String tokenUserKey = TOKEN_USER_PREFIX + token;
        
        try {
            // 获取用户当前的活跃token数量
            Set<Object> currentTokens = (Set<Object>) redisTemplate.opsForZSet().range(userTokensKey, 0, -1);
            int currentSessionCount = currentTokens != null ? currentTokens.size() : 0;
            
            // 检查是否超出最大会话数
            if (currentSessionCount >= maxSessions) {
                if (preventLoginIfMaximumExceeded) {
                    log.warn("用户 {} 已达到最大并发会话数 {}, 拒绝新的登录", username, maxSessions);
                    return false;
                } else {
                    // 移除最旧的token（FIFO策略）
                    removeOldestToken(username, currentTokens);
                }
            }
            
            // 注册新token
            redisTemplate.opsForZSet().add(userTokensKey, token, System.currentTimeMillis());
            redisTemplate.expire(userTokensKey, expirationSeconds, TimeUnit.SECONDS);
            
            // 建立token到用户的映射
            redisTemplate.opsForValue().set(tokenUserKey, username, expirationSeconds, TimeUnit.SECONDS);
            
            log.info("用户 {} 的token已注册，当前活跃会话数: {}", username, currentSessionCount + 1);
            return true;
            
        } catch (Exception e) {
            log.error("注册用户token失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 验证token是否有效（未被踢出且不在黑名单中）
     * @param token JWT token
     * @return 是否有效
     */
    public boolean isTokenValid(String token) {
        try {
            // 检查token是否在黑名单中
            if (isTokenInBlacklist(token)) {
                log.warn("Token在黑名单中: {}", token);
                return false;
            }
            
            String tokenUserKey = TOKEN_USER_PREFIX + token;
            String username = (String) redisTemplate.opsForValue().get(tokenUserKey);
            
            if (username == null) {
                return false;
            }
            
            String userTokensKey = USER_TOKENS_PREFIX + username;
            return redisTemplate.opsForZSet().score(userTokensKey, token) != null;
            
        } catch (Exception e) {
            log.error("验证token有效性失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 将token添加到黑名单
     * @param token JWT token
     * @param expirationSeconds token剩余过期时间（秒）
     */
    public void addTokenToBlacklist(String token, long expirationSeconds) {
        try {
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(blacklistKey, "1", expirationSeconds, TimeUnit.SECONDS);
            log.info("Token已添加到黑名单: {}", token);
        } catch (Exception e) {
            log.error("添加token到黑名单失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 检查token是否在黑名单中
     * @param token JWT token
     * @return 是否在黑名单中
     */
    public boolean isTokenInBlacklist(String token) {
        try {
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
            return redisTemplate.hasKey(blacklistKey);
        } catch (Exception e) {
            log.error("检查token是否在黑名单中失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 用户登出时移除token
     * @param token JWT token
     */
    public void removeUserToken(String token) {
        try {
            String tokenUserKey = TOKEN_USER_PREFIX + token;
            String username = (String) redisTemplate.opsForValue().get(tokenUserKey);
            
            if (username != null) {
                String userTokensKey = USER_TOKENS_PREFIX + username;
                redisTemplate.opsForZSet().remove(userTokensKey, token);
                redisTemplate.delete(tokenUserKey);
                
                log.info("用户 {} 的token已移除", username);
            }
            
        } catch (Exception e) {
            log.error("移除用户token失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 踢出用户的所有会话
     * @param username 用户名
     */
    public void kickOutAllUserSessions(String username) {
        try {
            String userTokensKey = USER_TOKENS_PREFIX + username;
            Set<Object> tokens = (Set<Object>) redisTemplate.opsForZSet().range(userTokensKey, 0, -1);
            
            if (tokens != null && !tokens.isEmpty()) {
                // 删除所有token到用户的映射
                for (Object token : tokens) {
                    String tokenUserKey = TOKEN_USER_PREFIX + token;
                    redisTemplate.delete(tokenUserKey);
                }
                
                // 删除用户的token集合
                redisTemplate.delete(userTokensKey);
                
                log.info("用户 {} 的所有会话已被踢出，共 {} 个", username, tokens.size());
            }
            
        } catch (Exception e) {
            log.error("踢出用户所有会话失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取用户当前活跃会话数
     * @param username 用户名
     * @return 活跃会话数
     */
    public int getUserActiveSessionCount(String username) {
        try {
            String userTokensKey = USER_TOKENS_PREFIX + username;
            Long size = redisTemplate.opsForZSet().zCard(userTokensKey);
            return size != null ? size.intValue() : 0;
            
        } catch (Exception e) {
            log.error("获取用户活跃会话数失败: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 移除最旧的token
     * 在实际应用中，可以根据token的创建时间来确定最旧的token
     */
    private void removeOldestToken(String username, Set<Object> currentTokens) {
        String userTokensKey = USER_TOKENS_PREFIX + username;
        Set<Object> oldestSet = (Set<Object>) redisTemplate.opsForZSet().range(userTokensKey, 0, 0);
        if (oldestSet != null && !oldestSet.isEmpty()) {
            String oldestToken = (String) oldestSet.iterator().next();
            removeUserToken(oldestToken);
            log.info("用户 {} 的最旧token已被移除以腾出空间", username);
        }
    }

    /**
     * 刷新token时更新Redis中的记录
     * @param oldToken 旧token
     * @param newToken 新token
     * @param expirationSeconds 过期时间（秒）
     */
    public void refreshUserToken(String oldToken, String newToken, long expirationSeconds) {
        try {
            String tokenUserKey = TOKEN_USER_PREFIX + oldToken;
            String username = (String) redisTemplate.opsForValue().get(tokenUserKey);
            
            if (username != null) {
                // 移除旧token
                removeUserToken(oldToken);
                
                // 注册新token
                registerUserToken(username, newToken, expirationSeconds);
                
                log.info("用户 {} 的token已刷新", username);
            }
            
        } catch (Exception e) {
            log.error("刷新用户token失败: {}", e.getMessage(), e);
        }
    }
}