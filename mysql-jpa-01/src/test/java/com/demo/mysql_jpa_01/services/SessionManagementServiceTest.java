package com.demo.mysql_jpa_01.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionManagementServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOps;

    @Mock
    private ZSetOperations<String, Object> zSetOps;

    @InjectMocks
    private SessionManagementService sessionManagementService;

    private final String username = "testuser";
    private final String token = "testtoken";
    private final long ttl = 3600L;

    @BeforeEach
    void setUp() {
        // 移除通用存根，将存根移到各个测试中
    }

    @Test
    void registerUserToken_success() {
        String userTokensKey = "user:tokens:" + username;
        String tokenUserKey = "token:user:" + token;
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(zSetOps.range(eq(userTokensKey), eq(0L), eq(-1L))).thenReturn(null);

        boolean result = sessionManagementService.registerUserToken(username, token, ttl);

        assertTrue(result);
        verify(zSetOps).add(eq(userTokensKey), eq(token), anyDouble());
        verify(redisTemplate).expire(eq(userTokensKey), eq(ttl), eq(TimeUnit.SECONDS));
        verify(valueOps).set(eq(tokenUserKey), eq(username), eq(ttl), eq(TimeUnit.SECONDS));
    }

    @Test
    void registerUserToken_maxSessionsExceeded() {
        ReflectionTestUtils.setField(sessionManagementService, "maxSessions", 5);
        ReflectionTestUtils.setField(sessionManagementService, "preventLoginIfMaximumExceeded", true);
        String userTokensKey = "user:tokens:" + username;
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(zSetOps.range(eq(userTokensKey), eq(0L), eq(-1L))).thenReturn(Set.of("t1", "t2", "t3", "t4", "t5"));

        boolean result = sessionManagementService.registerUserToken(username, token, ttl);

        assertFalse(result);
        verifyNoInteractions(valueOps);
    }

    @Test
    void isTokenValid_valid() {
        String tokenUserKey = "token:user:" + token;
        String userTokensKey = "user:tokens:" + username;
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(valueOps.get(eq(tokenUserKey))).thenReturn(username);
        when(zSetOps.score(eq(userTokensKey), eq(token))).thenReturn(1.0);

        boolean result = sessionManagementService.isTokenValid(token);

        assertTrue(result);
    }

    @Test
    void isTokenValid_invalid() {
        String tokenUserKey = "token:user:" + token;
        String userTokensKey = "user:tokens:" + username;
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(valueOps.get(eq(tokenUserKey))).thenReturn(username);
        when(zSetOps.score(eq(userTokensKey), eq(token))).thenReturn(null);

        boolean result = sessionManagementService.isTokenValid(token);

        assertFalse(result);
    }

    @Test
    void removeUserToken_success() {
        String tokenUserKey = "token:user:" + token;
        String userTokensKey = "user:tokens:" + username;
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(valueOps.get(eq(tokenUserKey))).thenReturn(username);

        sessionManagementService.removeUserToken(token);

        verify(zSetOps).remove(eq(userTokensKey), eq(token));
        verify(redisTemplate).delete(eq(tokenUserKey));
    }

    @Test
    void kickOutAllUserSessions_success() {
        String userTokensKey = "user:tokens:" + username;
        Set<Object> tokens = Set.of("token1", "token2");
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(zSetOps.range(eq(userTokensKey), eq(0L), eq(-1L))).thenReturn(tokens);

        sessionManagementService.kickOutAllUserSessions(username);

        verify(redisTemplate, times(2)).delete(startsWith("token:user:"));
        verify(redisTemplate).delete(eq(userTokensKey));
    }

    @Test
    void getUserActiveSessionCount_success() {
        String userTokensKey = "user:tokens:" + username;
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(zSetOps.zCard(eq(userTokensKey))).thenReturn(3L);

        int count = sessionManagementService.getUserActiveSessionCount(username);

        assertEquals(3, count);
    }

    @Test
    void refreshUserToken_success() {
        String newToken = "newToken";
        String tokenUserKey = "token:user:" + token;
        String newTokenUserKey = "token:user:" + newToken;
        String userTokensKey = "user:tokens:" + username;
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(valueOps.get(eq(tokenUserKey))).thenReturn(username);
        when(zSetOps.range(eq(userTokensKey), eq(0L), eq(-1L))).thenReturn(null);

        sessionManagementService.refreshUserToken(token, newToken, ttl);

        verify(zSetOps).remove(eq(userTokensKey), eq(token));
        verify(redisTemplate).delete(eq(tokenUserKey));
        verify(zSetOps).add(eq(userTokensKey), eq(newToken), anyDouble());
        verify(redisTemplate).expire(eq(userTokensKey), eq(ttl), eq(TimeUnit.SECONDS));
        verify(valueOps).set(eq(newTokenUserKey), eq(username), eq(ttl), eq(TimeUnit.SECONDS));
    }
}