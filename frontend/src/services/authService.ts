import api, { refreshToken, RefreshResponse } from './api';
import secureStorage from '../utils/secureStorage';
import tokenUtils from '../utils/tokenUtils';
import authErrorHandler from '../utils/authErrorHandler';

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface User {
  username: string;
  email?: string;
  authorities: string[];
}

export interface AuthResponse {
  success: boolean;
  accessToken?: string;
  user?: User;
  message?: string;
  errorCode?: string;
}

class AuthService {
  private normalizeUser(user: User | undefined | null, accessToken?: string): User | undefined {
    if (user) {
      return {
        username: user.username,
        email: user.email,
        authorities: user.authorities || []
      };
    }
    if (accessToken) {
      const tokenUser = tokenUtils.getUserFromToken(accessToken);
      if (tokenUser) {
        return {
          username: tokenUser.username,
          email: undefined,
          authorities: tokenUser.roles || []
        };
      }
    }
    return undefined;
  }

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

  async login(loginData: LoginRequest): Promise<AuthResponse> {
    try {
      const response = await api.post('/auth/login', loginData, {
        withCredentials: true
      });
      const data = response.data;
        
      if (data.success && data.accessToken) {
        secureStorage.saveTokens(data.accessToken);
          
        const userInfo = this.normalizeUser(data.user, data.accessToken);

        if (userInfo) {
          secureStorage.saveUserInfo(userInfo);
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

  async logout(): Promise<void> {
    try {
      await api.post('/auth/logout', {}, { withCredentials: true });
    } catch (error) {
      console.error('登出请求失败:', error);
    } finally {
      secureStorage.clearTokens();
      secureStorage.clearUserInfo();
    }
  }

  async refreshToken(): Promise<AuthResponse> {
    try {
      const data: RefreshResponse = await refreshToken();
      
      if (data.accessToken) {
        secureStorage.saveTokens(data.accessToken);
        
        const userInfo = this.normalizeUser(data.user as User | undefined, data.accessToken);
        
        if (userInfo) {
          secureStorage.saveUserInfo(userInfo);
        }
        
        return { success: true, accessToken: data.accessToken, user: userInfo };
      }
      return { success: false, message: 'Refresh failed' };
    } catch (error: any) {
      authErrorHandler.handleAuthError(true);
      return { 
        success: false, 
        message: error.response?.data?.message || 'Session expired' 
      };
    }
  }

  isAuthenticated(): boolean {
    const token = secureStorage.getAccessToken();
    return !!token;
  }
  
  getCurrentUser(): User | null {
    return secureStorage.getUserInfo();
  }
  
  getAccessToken(): string | null {
    return secureStorage.getAccessToken();
  }
}

const authService = new AuthService();
export default authService;
