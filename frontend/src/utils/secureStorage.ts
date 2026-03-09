/**
 * 安全存储工具类
 * 
 * 设计原则：
 * 1. AccessToken 存储在 sessionStorage 中（会话级，关闭浏览器自动清除）
 * 2. RefreshToken 通过 HttpOnly Cookie 存储（后端设置，JavaScript 无法访问）
 * 3. 用户信息存储在 localStorage 中（非敏感数据）
 * 
 * 安全性提升：
 * - 防止 XSS 攻击窃取 RefreshToken
 * - 会话级 AccessToken 降低长期暴露风险
 * - 使用 Secure 和 SameSite 属性防止 CSRF
 */

class SecureStorage {
  private ACCESS_TOKEN_KEY = 'access_token';
  private USERNAME_KEY = 'username';
  private AUTHORITIES_KEY = 'authorities';
  private EMAIL_KEY = 'email';

  /**
   * 保存 Token
   * @param accessToken 访问令牌（存储在 sessionStorage）
   */
  public saveTokens(accessToken: string): void {
    // AccessToken 存储到 sessionStorage（会话级）
    sessionStorage.setItem(this.ACCESS_TOKEN_KEY, accessToken);
    
    // RefreshToken 不需要前端处理，后端会通过 Set-Cookie 头设置 HttpOnly Cookie
    console.log('Token 已保存，RefreshToken 已通过 HttpOnly Cookie 存储');
  }

  /**
   * 获取访问令牌
   * @returns AccessToken 或 null
   */
  public getAccessToken(): string | null {
    return sessionStorage.getItem(this.ACCESS_TOKEN_KEY);
  }

  /**
   * 清除所有 Token
   * 注意：RefreshToken 的清除需要调用后端的 logout API
   */
  public clearTokens(): void {
    sessionStorage.removeItem(this.ACCESS_TOKEN_KEY);
  }

  /**
   * 检查是否已认证（有 AccessToken）
   * @returns boolean
   */
  public isAuthenticated(): boolean {
    const token = this.getAccessToken();
    return !!token;
  }

  /**
   * 保存用户基本信息（非敏感数据）
   */
  public saveUserInfo(username: string, email: string, authorities: string[]): void {
    localStorage.setItem(this.USERNAME_KEY, username);
    localStorage.setItem(this.EMAIL_KEY, email);
    localStorage.setItem(this.AUTHORITIES_KEY, JSON.stringify(authorities));
  }

  /**
   * 获取当前用户信息
   */
  public getCurrentUser(): { username: string; email: string; authorities: string[] } | null {
    const username = localStorage.getItem(this.USERNAME_KEY);
    const email = localStorage.getItem(this.EMAIL_KEY);
    const authoritiesStr = localStorage.getItem(this.AUTHORITIES_KEY);
    
    if (!username || !authoritiesStr) {
      return null;
    }

    try {
      const authorities = JSON.parse(authoritiesStr);
      return {
        username,
        email: email || '',
        authorities
      };
    } catch (error) {
      console.error('解析用户信息失败:', error);
      return null;
    }
  }

  /**
   * 清除用户信息
   */
  public clearUserInfo(): void {
    localStorage.removeItem(this.USERNAME_KEY);
    localStorage.removeItem(this.EMAIL_KEY);
    localStorage.removeItem(this.AUTHORITIES_KEY);
  }

  /**
   * 解码 JWT Token（用于检查过期时间等）
   */
  public decodeToken(token: string): any {
    try {
      const base64Url = token.split('.')[1];
      if (!base64Url) {
        return null;
      }
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        window.atob(base64)
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch (e) {
      console.error('Token 解码失败:', e);
      return null;
    }
  }

  /**
   * 检查 Token 是否即将过期
   * @param token Token 字符串
   * @param thresholdMinutes 阈值（分钟），默认 5 分钟
   * @returns boolean
   */
  public isTokenExpiringSoon(token: string, thresholdMinutes: number = 5): boolean {
    const decoded = this.decodeToken(token);
    if (!decoded || !decoded.exp) {
      return false;
    }

    const now = Date.now() / 1000;
    const timeUntilExpiry = decoded.exp - now;
    
    return timeUntilExpiry < thresholdMinutes * 60;
  }

  /**
   * 检查 Token 是否已过期
   * @param token Token 字符串
   * @returns boolean
   */
  public isTokenExpired(token: string): boolean {
    const decoded = this.decodeToken(token);
    if (!decoded || !decoded.exp) {
      return true;
    }

    return Date.now() / 1000 >= decoded.exp;
  }
}

// 导出单例
const secureStorage = new SecureStorage();
export default secureStorage;
