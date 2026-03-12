import secureStorage from '../utils/secureStorage';

export const authErrorHandler = {
  handleAuthError: (redirectToLogin: boolean = true) => {
    secureStorage.clearTokens();
    secureStorage.clearUserInfo();
    
    if (redirectToLogin) {
      window.location.href = '/login';
    }
  }
};

export default authErrorHandler;
