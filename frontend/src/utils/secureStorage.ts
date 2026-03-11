// 仅负责存储，Token 解析逻辑移至 tokenUtils.ts
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
  
  saveUserInfo: (user: any) => {
    sessionStorage.setItem('user', JSON.stringify(user));
  },
  
  getUserInfo: () => {
    const userStr = sessionStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },
  
  clearUserInfo: () => {
    sessionStorage.removeItem('user');
  }
};

export default secureStorage;
