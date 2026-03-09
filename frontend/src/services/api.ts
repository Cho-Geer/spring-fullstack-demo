import axios from 'axios';
import secureStorage from '../utils/secureStorage';

// 创建 axios 实例
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // 允许跨域携带 Cookie（用于发送和接收 HttpOnly Cookie）
});

// 请求拦截器 - 添加 JWT token
api.interceptors.request.use(
  (config) => {
    // 从 sessionStorage 获取 AccessToken
    const token = secureStorage.getAccessToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器 - 处理 token 过期
api.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    const originalRequest = error.config;
    
    // 如果是 401 错误且未重试过，尝试刷新 token
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      try {
        // 调用刷新 token 接口，RefreshToken 在 HttpOnly Cookie 中
        const response = await axios.post(
          'http://localhost:8080/api/auth/refresh',
          {},
          { withCredentials: true }
        );
        
        const { accessToken } = response.data;
        
        // 更新 sessionStorage 中的 AccessToken
        secureStorage.saveTokens(accessToken);
        
        // 重新发送原始请求
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        // 刷新 token 失败，清除本地存储并跳转到登录页
        secureStorage.clearTokens();
        secureStorage.clearUserInfo();
        window.location.href = '/login';
      }
    }
    
    return Promise.reject(error);
  }
);

export default api;