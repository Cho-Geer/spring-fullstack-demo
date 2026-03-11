import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import authService, { User } from '../services/authService';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (username: string, password: string) => Promise<{ success: boolean; message: string }>;
  register: (username: string, email: string, password: string) => Promise<{ success: boolean; message: string }>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // 组件挂载时检查用户是否已登录，尝试从 HttpOnly Cookie 恢复会话
    const initializeAuth = async () => {
      try {
        // 1. 先检查本地是否有 Token
        if (authService.isAuthenticated()) {
          const currentUser = authService.getCurrentUser();
          setUser(currentUser);
        } else {
          // 2. 如果本地没有 Token，尝试调用 refresh 接口利用 HttpOnly Cookie 恢复
          try {
            const response = await authService.refreshToken();
            if (response.success && response.accessToken) {
               // refreshToken 成功后，authService 内部已经更新了 Token
               const currentUser = authService.getCurrentUser();
               setUser(currentUser);
            }
          } catch (e) {
             // 恢复失败，无需操作，保持未登录状态
             console.log('Session restore failed:', e);
          }
        }
      } catch (error) {
        console.error('Auth initialization error:', error);
      } finally {
        setIsLoading(false);
      }
    };

    initializeAuth();
  }, []);

  const login = async (username: string, password: string): Promise<{ success: boolean; message: string }> => {
    try {
      setIsLoading(true);
      const response = await authService.login({ username, password });
      
      if (response.success) {
        const currentUser = authService.getCurrentUser();
        setUser(currentUser);
        return { success: true, message: '登录成功' };
      } else {
        return { success: false, message: response.message || '登录失败' };
      }
    } catch (error) {
      console.error('登录错误:', error);
      return { success: false, message: '登录过程中发生错误' };
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (username: string, email: string, password: string): Promise<{ success: boolean; message: string }> => {
    try {
      setIsLoading(true);
      const response = await authService.register({ username, email, password });
      
      if (response.success) {
        return { success: true, message: '注册成功，请登录' };
      } else {
        return { success: false, message: response.message || '注册失败' };
      }
    } catch (error) {
      console.error('注册错误:', error);
      return { success: false, message: '注册过程中发生错误' };
    } finally {
      setIsLoading(false);
    }
  };

  const logout = async (): Promise<void> => {
    try {
      setIsLoading(true);
      await authService.logout();
      setUser(null);
    } catch (error) {
      console.error('登出错误:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const value: AuthContextType = {
    user,
    isAuthenticated: !!user,
    isLoading,
    login,
    register,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};