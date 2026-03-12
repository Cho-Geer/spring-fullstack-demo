import { User } from '../services/authService';

const STORAGE_KEYS = {
  ACCESS_TOKEN: 'accessToken',
  USER: 'user'
} as const;

const secureStorage = {
  saveTokens: (accessToken: string) => {
    sessionStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, accessToken);
  },

  getAccessToken: (): string | null => {
    return sessionStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);
  },

  clearTokens: () => {
    sessionStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
  },
  
  saveUserInfo: (user: User): void => {
    sessionStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(user));
  },
  
  getUserInfo: (): User | null => {
    const userStr = sessionStorage.getItem(STORAGE_KEYS.USER);
    if (!userStr) return null;
    try {
      return JSON.parse(userStr) as User;
    } catch {
      return null;
    }
  },
  
  clearUserInfo: () => {
    sessionStorage.removeItem(STORAGE_KEYS.USER);
  },

  clearAll: (): void => {
    secureStorage.clearTokens();
    secureStorage.clearUserInfo();
  }
};

export default secureStorage;
export { STORAGE_KEYS };
