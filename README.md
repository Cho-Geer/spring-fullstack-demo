# Spring Boot + React Fullstack Demo

A robust full-stack application demonstrating secure authentication flows, including HttpOnly Cookie-based refresh tokens, token rotation, and strict security configurations suitable for production environments.

## 🚀 Key Features

- **Secure Authentication**:
  - **HttpOnly Cookie**: Refresh tokens are stored in HttpOnly cookies to prevent XSS attacks.
  - **Token Rotation**: Refresh tokens are rotated on every use to detect token theft.
  - **Session Management**: Access tokens are stored in `sessionStorage` (tab-specific).
  - **Blacklisting**: Active tokens are blacklisted on logout using Redis.
- **Strict Security**:
  - **CORS**: Configurable strict CORS policies (no wildcards in production).
  - **Secrets Management**: Sensitive data managed via environment variables.
  - **CSRF**: Disabled for stateless JWT flow (future enhancements planned for cookie endpoints).

## 🛠 Tech Stack

- **Backend**: Spring Boot 3.x, Spring Security 6, JPA, Redis, MySQL/H2
- **Frontend**: React 18, TypeScript, Axios, Context API
- **Testing**: JUnit 5, MockMvc, TestContainers (integration tests)

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
   - `JWT_EXPIRATION`: Access token expiration time in milliseconds (default: 3600000)
   - `JWT_REFRESH_EXPIRATION`: Refresh token expiration time in milliseconds (default: 86400000)

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
3. **Refresh**: When access token expires (401), client calls `/refresh`. Server validates cookie, rotates tokens, and returns new pair.
4. **Logout**: Client calls `/logout`. Server invalidates cookie and blacklists access token in Redis.

### Trade-offs & Limitations
- **CSRF**: Currently disabled. While HttpOnly cookies are used for refresh tokens, the refresh endpoint is POST and technically vulnerable to CSRF if not properly protected (e.g., via SameSite=Strict). Future updates will enforce CSRF tokens for cookie-based endpoints.
- **Statelessness**: While JWTs are stateless, the revocation list (blacklist) requires Redis state.

## 🧪 Testing

Run integration tests using Maven:
```bash
./mvnw test
```
Tests use H2 database and embedded Redis configuration for isolation.

## 📝 License
MIT
