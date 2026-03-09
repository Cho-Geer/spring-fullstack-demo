import api from './api';
import secureStorage from '../utils/secureStorage';

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  success: boolean;
  message: string;
  accessToken?: string;
  refreshToken?: string;
  tokenType?: string;
  expiresIn?: number;
  username?: string;
  authorities?: string[];
  errorCode?: string;
}

export interface User {
  username: string;
  email: string;
  authorities: string[];
}

class AuthService {
  // 用户注册
  async register(registerData: RegisterRequest): Promise<AuthResponse> {
    try {
      const response = await api.post('/auth/register', registerData);
      return response.data;
    } catch (error: any) {
      if (error.response?.data) {
        return error.response.data;
      }
      return {
        success: false,
        message: '网络错误，请稍后重试',
        errorCode: 'NETWORK_ERROR'
      };
    }
  }

  // 用户登录
  async login(loginData: LoginRequest): Promise<AuthResponse> {
    try {
      const response = await api.post('/auth/login', loginData, {
        withCredentials: true // 允许发送 Cookie，接收 HttpOnly Cookie
      });
      const data = response.data;
        
      if (data.success && data.accessToken) {
        // 使用安全的存储方式，只存储accessToken
        secureStorage.saveTokens(data.accessToken);
          
        // 保存非敏感的用户信息到 localStorage
        if (data.username && data.authorities) {
          secureStorage.saveUserInfo(
            data.username,
            data.email || '',
            data.authorities
          );
        }
      }
        
      return data;
    } catch (error: any) {
      if (error.response?.data) {
        return error.response.data;
      }
      return {
        success: false,
        message: '网络错误，请稍后重试',
        errorCode: 'NETWORK_ERROR'
      };
    }
  }

  // 用户登出
  async logout(): Promise<void> {
    try {
      // 发送登出请求，后端会清除 HttpOnly Cookie
      await api.post('/auth/logout', {}, { withCredentials: true });
    } catch (error) {
      console.error('登出请求失败:', error);
    } finally {
      // 无论请求是否成功，都清除本地存储
      secureStorage.clearTokens();
      secureStorage.clearUserInfo();
    }
  }

  // 刷新 token
  async refreshToken(): Promise<AuthResponse> {
    try {
      // RefreshToken 在 HttpOnly Cookie 中，后端会自动读取
      const response = await api.post('/auth/refresh', {}, {
        withCredentials: true
      });
        
      const data = response.data;
      if (data.success && data.accessToken) {
        // 更新 AccessToken
        secureStorage.saveTokens(data.accessToken);
      }
        
      return data;
    } catch (error: any) {
      secureStorage.clearTokens();
      secureStorage.clearUserInfo();
      if (error.response?.data) {
        return error.response.data;
      }
      return {
        success: false,
        message: '刷新 token 失败',
        errorCode: 'REFRESH_FAILED'
      };
    }
  }

  // 检查是否已登录
  isAuthenticated(): boolean {
    return secureStorage.isAuthenticated();
  }
  
  // 获取当前用户信息
  getCurrentUser(): User | null {
    return secureStorage.getCurrentUser();
  }
  
  // 获取访问 token
  getAccessToken(): string | null {
    return secureStorage.getAccessToken();
  }
}

const authService = new AuthService();
export default authService;