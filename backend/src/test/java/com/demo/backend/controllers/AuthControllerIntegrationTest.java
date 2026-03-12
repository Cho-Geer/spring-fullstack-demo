package com.demo.backend.controllers;

import com.demo.backend.BackendApplication;
import com.demo.backend.services.SessionManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.demo.backend.repositories.UserRepository userRepository;
    
    @MockBean
    private SessionManagementService sessionManagementService;

    @Test
    void login_success() throws Exception {
        // 模拟SessionManagementService的方法，让登录成功
        when(sessionManagementService.registerUserToken(anyString(), anyString(), anyLong())).thenReturn(true);
        when(sessionManagementService.isTokenInBlacklist(anyString())).thenReturn(false);
        
        // 先检查用户是否存在，如果存在则删除
        userRepository.findByUsername("testuser").ifPresent(userRepository::delete);
        userRepository.findByEmail("test@example.com").ifPresent(userRepository::delete);
        
        // 先注册用户
        Map<String, String> registerRequest = Map.of(
                "username", "testuser",
                "email", "test@example.com",
                "password", "Password123!"
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andDo(result -> System.out.println("Register response: " + result.getResponse().getContentAsString()));


        // 然后登录
        Map<String, String> loginRequest = Map.of("username", "testuser", "password", "Password123!");

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(result -> System.out.println(result.getResponse().getContentAsString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));
    }

    @Test
    void login_failure_invalidCredentials() throws Exception {
        Map<String, String> loginRequest = Map.of("username", "testuser", "password", "wrongpassword");

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    // 可以添加更多测试，如需要认证的端点
    @Test
    void protectedEndpoint_withoutToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/users"))
                .andExpect(status().isUnauthorized());
    }
}