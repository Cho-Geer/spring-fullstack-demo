import { jwtDecode } from 'jwt-decode';

interface DecodedToken {
  sub: string;
  exp: number;
  iat: number;
  roles: string[];
}

export interface TokenUser {
  username: string;
  roles: string[];
}

const tokenUtils = {
  decodeToken: (token: string): DecodedToken | null => {
    try {
      return jwtDecode<DecodedToken>(token);
    } catch (error) {
      console.error('Error decoding token:', error);
      return null;
    }
  },

  isTokenExpired: (token: string): boolean => {
    const decoded = tokenUtils.decodeToken(token);
    if (!decoded) return true;
    const currentTime = Date.now() / 1000;
    return decoded.exp < currentTime;
  },
  
  getUserFromToken: (token: string): TokenUser | null => {
    const decoded = tokenUtils.decodeToken(token);
    if (!decoded) return null;
    return {
      username: decoded.sub,
      roles: decoded.roles || []
    };
  }
};

export default tokenUtils;
