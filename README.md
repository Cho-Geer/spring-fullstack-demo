# Spring Boot + React Fullstack Demo

A production-oriented full-stack demo application demonstrating secure authentication patterns with HttpOnly Cookie-based refresh tokens, session management, and configurable security settings.

## 🚀 Key Features

- **Secure Authentication**:
  - **HttpOnly Cookie**: Refresh tokens stored in HttpOnly cookies to prevent XSS attacks.
  - **Session Management**: Access tokens stored in `sessionStorage` (tab-specific).
  - **Blacklisting**: Active tokens blacklisted on logout using Redis.
- **Environment-Based Security**:
  - **CORS**: Configurable per environment (dev/test/prod profiles).
  - **Cookie Attributes**: `secure` and `SameSite` vary by environment.
  - **Secrets**: JWT_SECRET managed via environment variables.

## 🛠 Tech Stack

- **Backend**: Spring Boot 3.x, Spring Security 6, JPA, Redis, MySQL/H2
- **Frontend**: React 18, TypeScript, Axios, Context API
- **Testing**: JUnit 5, MockMvc, TestContainers-ready

## 🏃‍♂️ Getting Started

### Prerequisites
- Java 17+
- Node.js 18+
- Docker (optional, for Redis/MySQL)

### Backend Setup

1. **Configure Environment**:
   Set the required environment variables. JWT_SECRET is mandatory and must be at least 32 bytes long.
   ```bash
   export JWT_SECRET=your_secure_random_secret_key_at_least_32_bytes
   export CORS_ALLOWED_ORIGINS=http://localhost:3000
   ```
   
   **Required Environment Variables**:
   - `JWT_SECRET`: Secure random secret key (at least 32 bytes)
   - `CORS_ALLOWED_ORIGINS`: Frontend application origin(s)
   
   **Optional Environment Variables**:
   - `JWT_EXPIRATION`: Access token expiration in milliseconds (default: 3600000)
   - `JWT_REFRESH_EXPIRATION`: Refresh token expiration in milliseconds (default: 86400000)

2. **Run with Maven**:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

### Frontend Setup

1. **Configure Environment**:
   Create a `.env` file in the `frontend` directory:
   ```env
   REACT_APP_API_BASE_URL=http://localhost:8080/api
   ```

2. **Install & Run**:
   ```bash
   cd frontend
   npm install
   npm start
   ```

## 🔒 Security Implementation Details

### Authentication Flow
1. **Login**: Client sends credentials. Server validates and returns:
   - `accessToken` (JSON body) -> Stored in `sessionStorage`.
   - `refreshToken` (HttpOnly Cookie) -> Automatically handled by browser.
2. **Access**: Client sends `Authorization: Bearer <token>` header.
3. **Refresh**: When access token expires (401), client calls `/refresh`. Server validates cookie and returns new access token.
4. **Logout**: Client calls `/logout`. Server invalidates cookie and blacklists access token in Redis.

### Cookie Security by Environment
- **Development**: `secure=false`, `SameSite=Lax` (allows HTTP for localhost)
- **Test**: `secure=false`, `SameSite=Strict` (stricter CSRF protection)
- **Production**: `secure=true`, `SameSite=Strict` (HTTPS required)

### Current Status

#### Implemented
- HttpOnly Cookie for refresh tokens
- Access token in sessionStorage
- Redis-based token blacklist on logout
- Environment-specific cookie attributes (secure/SameSite)
- CORS configuration via environment variables
- JWT_SECRET required via environment variable

#### Planned Improvements
- Token rotation on every refresh (currently: refresh token valid for 7 days)
- CSRF token integration for additional protection
- Rate limiting on auth endpoints

### Trade-offs & Limitations
- **CSRF**: Disabled for stateless JWT flow. Mitigated by SameSite=Strict in production. Consider adding CSRF tokens for higher security requirements.
- **Token Rotation**: Currently refresh tokens are valid for 7 days. Adding rotation on each use provides theft detection but increases complexity.
- **Statelessness**: JWTs are stateless, but token revocation requires Redis state.
- **Session Binding**: Refresh tokens are bound to the original device/session.

## 🧪 Testing

Run integration tests using Maven:
```bash
./mvnw test
```
Tests use H2 database and embedded Redis configuration for isolation.

## 📝 License
MIT
