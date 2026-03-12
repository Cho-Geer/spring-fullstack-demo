import { User } from '../services/authService';

const secureStorage = {
  saveTokens: (accessToken: string) => {
    sessionStorage.setItem('accessToken', accessToken);
  },

  getAccessToken: () => {
    return sessionStorage.getItem('accessToken');
  },

  clearTokens: () => {
    sessionStorage.removeItem('accessToken');
  },
  
  saveUserInfo: (user: User) => {
    sessionStorage.setItem('user', JSON.stringify(user));
  },
  
  getUserInfo: (): User | null => {
    const userStr = sessionStorage.getItem('user');
    if (!userStr) return null;
    try {
      return JSON.parse(userStr) as User;
    } catch {
      return null;
    }
  },
  
  clearUserInfo: () => {
    sessionStorage.removeItem('user');
  }
};

export default secureStorage;
