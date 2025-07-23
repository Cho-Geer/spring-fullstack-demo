import api from './api';

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
      const response = await api.post('/auth/login', loginData);
      const data = response.data;
      
      if (data.success && data.accessToken) {
        // 保存token到localStorage
        localStorage.setItem('accessToken', data.accessToken);
        localStorage.setItem('refreshToken', data.refreshToken);
        localStorage.setItem('username', data.username);
        localStorage.setItem('authorities', JSON.stringify(data.authorities));
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
      await api.post('/auth/logout');
    } catch (error) {
      console.error('登出请求失败:', error);
    } finally {
      // 无论请求是否成功，都清除本地存储
      this.clearLocalStorage();
    }
  }

  // 刷新token
  async refreshToken(): Promise<AuthResponse> {
    try {
      const refreshToken = localStorage.getItem('refreshToken');
      if (!refreshToken) {
        throw new Error('No refresh token available');
      }
      
      const response = await api.post('/auth/refresh', {
        refreshToken: refreshToken
      });
      
      const data = response.data;
      if (data.success && data.accessToken) {
        localStorage.setItem('accessToken', data.accessToken);
      }
      
      return data;
    } catch (error: any) {
      this.clearLocalStorage();
      if (error.response?.data) {
        return error.response.data;
      }
      return {
        success: false,
        message: '刷新token失败',
        errorCode: 'REFRESH_FAILED'
      };
    }
  }

  // 检查是否已登录
  isAuthenticated(): boolean {
    const token = localStorage.getItem('accessToken');
    return !!token;
  }

  // 获取当前用户信息
  getCurrentUser(): User | null {
    const username = localStorage.getItem('username');
    const email = localStorage.getItem('email');
    const authoritiesStr = localStorage.getItem('authorities');
    
    if (username && authoritiesStr) {
      try {
        const authorities = JSON.parse(authoritiesStr);
        return {
          username,
          email: email || '',
          authorities
        };
      } catch (error) {
        console.error('解析用户权限失败:', error);
      }
    }
    
    return null;
  }

  // 获取访问token
  getAccessToken(): string | null {
    return localStorage.getItem('accessToken');
  }

  // 清除本地存储
  private clearLocalStorage(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('username');
    localStorage.removeItem('email');
    localStorage.removeItem('authorities');
  }
}

const authService = new AuthService();
export default authService;