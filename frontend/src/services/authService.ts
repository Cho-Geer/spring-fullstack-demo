import api from './api';
import secureStorage from '../utils/secureStorage';
import tokenUtils from '../utils/tokenUtils';

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
  accessToken?: string;
  refreshToken?: string;
  user?: any;
  message?: string;
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
          
        // 如果后端没有返回 user 对象，尝试从 token 中解析
        let userInfo = data.user;
        if (!userInfo) {
          userInfo = tokenUtils.getUserFromToken(data.accessToken);
        }

        // 保存非敏感的用户信息到 localStorage
        if (userInfo) {
          secureStorage.saveUserInfo(userInfo);
          // 确保返回的数据中包含 user 对象，以便 AuthContext 正确更新状态
          data.user = userInfo;
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

  // 刷新 Token
  async refreshToken(): Promise<AuthResponse> {
    try {
      const response = await api.post('/auth/refresh');
      const { accessToken, user } = response.data;
      if (accessToken) {
        secureStorage.saveTokens(accessToken);
        return { success: true, accessToken, user };
      }
      return { success: false, message: 'Refresh failed' };
    } catch (error: any) {
        secureStorage.clearTokens();
        secureStorage.clearUserInfo();
        return { 
            success: false, 
            message: error.response?.data?.message || 'Session expired' 
        };
    }
  }

  // 检查是否已登录
  isAuthenticated(): boolean {
    const token = secureStorage.getAccessToken();
    return !!token;
  }
  
  // 获取当前用户信息
  getCurrentUser(): User | null {
    return secureStorage.getUserInfo();
  }
  
  // 获取访问 token
  getAccessToken(): string | null {
    return secureStorage.getAccessToken();
  }
}

const authService = new AuthService();
export default authService;