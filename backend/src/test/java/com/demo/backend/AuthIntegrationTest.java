package com.demo.backend;

import com.demo.backend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test") // 使用 test profile (H2 + Embedded Redis)
@Transactional // 确保每个测试方法结束后回滚事务
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;
    
    @MockBean
    private RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void setupRedisMock() {
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        ZSetOperations<String, String> zSetOperations = mock(ZSetOperations.class);
        
        // 模拟 opsForValue
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        // 模拟 opsForZSet
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        
        // 模拟 SessionManagementService.registerUserToken 中的调用
        when(zSetOperations.range(anyString(), anyLong(), anyLong())).thenReturn(new HashSet<>());
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        doAnswer(invocation -> null).when(valueOperations).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    void testRegisterAndLoginFlow() throws Exception {
        // 0. Cleanup (防止脏数据)
        if (userRepository.existsByUsername("testuser")) {
            userRepository.delete(userRepository.findByUsername("testuser").get());
        }

        // 1. Register
        String registerJson = """
            {
                "username": "testuser",
                "email": "test@example.com",
                "password": "Password123!"
            }
            """;

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
                .andExpect(status().isCreated()) // 注册成功返回201 Created
                .andExpect(jsonPath("$.success").value(true));

        // 2. Login
        String loginJson = """
            {
                "username": "testuser",
                "password": "Password123!"
            }
            """;

        // 注意：在测试环境中，如果Redis未正确配置，SessionManagementService可能会抛出异常
        // 但由于我们使用了test profile和Embedded Redis（或mock），应该能正常工作
        // 如果返回500，可能是Redis连接问题。但在集成测试中，我们期望它工作。
        
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.accessToken").exists())
                // H2/Test环境也应该模拟Cookie行为
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().httpOnly("refresh_token", true));
    }
}
