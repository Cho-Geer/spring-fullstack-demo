# Spring Boot + React 全栈演示项目

一个**面向生产环境**的全栈演示应用,展示了基于 HttpOnly Cookie 的 Refresh Token 安全认证模式、会话管理以及可配置的安全设置。

## 🚀 核心特性

- **安全认证**:
  - **HttpOnly Cookie**:Refresh Token 存储在 HttpOnly Cookie 中,以防止 XSS 攻击。
  - **会话管理**:Access Token 存储在 `sessionStorage` 中(按 Tab 隔离)。
  - **黑名单机制**:登出时使用 Redis 将活跃 Token 加入黑名单。
- **基于环境的安全配置**:
  - **CORS**:可按环境(dev / test / prod profile)进行配置。
  - **Cookie 属性**:`secure` 和 `SameSite` 随环境不同而变化。
  - **密钥管理**:`JWT_SECRET` 通过环境变量管理。

## 🏗 架构概览

### 各组件的职责

- **后端(Spring Boot)**:签发 JWT Token、校验凭证、管理会话状态、执行安全策略,并在 Redis 中处理 Token 黑名单。
- **前端(React)**:管理 UI 状态,将 Access Token 存储在 sessionStorage 中,在遇到 401 时触发刷新,并处理登录/登出的 UI 流程。
- **Redis**:存储用于即时吊销的 Token 黑名单,同时作为活跃会话的会话状态存储。

### Token 职责划分

- **Access Token**:短期有效(1 小时),用于 API 鉴权,存储在 sessionStorage 中以避免被 XSS 访问。
- **Refresh Token**:长期有效(7 天),用于获取新的 Access Token,存储在 HttpOnly Cookie 中以避免被 XSS 窃取。

### 为什么选择 sessionStorage?

我们没有选择 `localStorage`,而是选择 `sessionStorage`,是因为它**按 Tab 隔离**并且**在 Tab 关闭时被清除**。这可以提供更好的会话间安全隔离,降低 Token 在不同浏览上下文之间被复用的风险。同时,它还能自动实现许多应用所期望的"关闭浏览器即登出"的行为。

### 为什么需要黑名单?

JWT 本身是无状态的,但我们仍然需要**即时吊销会话**的能力(例如:用户点击登出、管理员强制结束会话、检测到安全事件等)。基于 Redis 的黑名单可以在 Token 校验时提供 O(1) 查找,让我们在"以无状态为主"和"必要时即时吊销"之间兼得。

## 🛠 技术栈

- **后端**:Spring Boot 3.x、Spring Security 6、JPA、Redis、MySQL/H2
- **前端**:React 18、TypeScript、Axios、Context API
- **测试**:JUnit 5、MockMvc、TestContainers 就绪

## 🏃‍♂️ 快速开始

### 环境要求
- Java 17 及以上
- Node.js 18 及以上
- Docker(可选,用于 Redis/MySQL)

### 后端设置

1. **配置环境变量**:
   设置所需的环境变量。`JWT_SECRET` 是必填项,长度至少需要 32 字节。
   ```bash
   export JWT_SECRET=your_secure_random_secret_key_at_least_32_bytes
   export CORS_ALLOWED_ORIGINS=http://localhost:3000
   ```
   
   **必填环境变量**:
   - `JWT_SECRET`:安全的随机密钥(至少 32 字节)
   - `CORS_ALLOWED_ORIGINS`:前端应用的来源
   
   **可选环境变量**:
   - `JWT_EXPIRATION`:Access Token 过期时间(毫秒,默认:3600000)
   - `JWT_REFRESH_EXPIRATION`:Refresh Token 过期时间(毫秒,默认:86400000)

2. **使用 Maven 运行**:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

### 前端设置

1. **配置环境变量**:
   在 `frontend` 目录下创建一个 `.env` 文件:
   ```env
   REACT_APP_API_BASE_URL=http://localhost:8080/api
   ```

2. **安装与运行**:
   ```bash
   cd frontend
   npm install
   npm start
   ```

## 🔒 安全实现细节

### 认证流程
1. **登录**:客户端提交凭证。服务端校验成功后返回:
   - `accessToken`(放在 JSON Body 中)→ 存储在 `sessionStorage`。
   - `refreshToken`(HttpOnly Cookie)→ 由浏览器自动处理。
2. **访问接口**:客户端在请求头携带 `Authorization: Bearer <token>`。
3. **刷新**:当 Access Token 过期(收到 401)时,客户端调用 `/refresh`,服务端校验 Cookie 后返回新的 Access Token。
4. **登出**:客户端调用 `/logout`,服务端清除 Cookie 并将 Access Token 加入 Redis 黑名单。

### 按环境区分的 Cookie 安全策略
- **Development**:`secure=false`、`SameSite=Lax`(允许 localhost 使用 HTTP)
- **Test**:`secure=false`、`SameSite=Strict`(更严格的 CSRF 防护)
- **Production**:`secure=true`、`SameSite=Strict`(强制 HTTPS)

### 当前状态

#### 已实现
- 用于 Refresh Token 的 HttpOnly Cookie
- 存储在 sessionStorage 中的 Access Token
- 登出时基于 Redis 的 Token 黑名单
- 按环境区分的 Cookie 属性(secure / SameSite)
- 通过环境变量配置 CORS
- 通过环境变量强制要求 `JWT_SECRET`

#### 计划改进
- 每次刷新都进行 Token 轮换(当前:Refresh Token 7 天有效)
- 引入 CSRF Token 以提供额外保护
- 对认证接口加入限流

### 权衡与限制
- **CSRF**:为保持无状态 JWT 流程而被禁用。在生产环境中通过 `SameSite=Strict` 缓解。如有更高安全要求,可考虑加入 CSRF Token。
- **Token 轮换**:目前 Refresh Token 7 天内有效。在每次使用时进行轮换可以检测泄露,但会增加复杂度。
- **无状态性**:JWT 本身无状态,但 Token 吊销需要 Redis 状态。
- **会话绑定**:Refresh Token 与原始设备 / 会话绑定。

## 🧪 测试

使用 Maven 运行集成测试:
```bash
./mvnw test
```
测试使用 H2 数据库和内嵌 Redis 配置以保证隔离性。

## 📝 许可证
MIT

---

## 🇯🇵 日本語 | 🇬🇧 English

- [日本語版](./README.md)
- [English version](./README.en.md)
