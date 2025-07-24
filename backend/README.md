# Backend Demo Project

## 概述
这是一个使用 Spring Boot、MySQL 和 JPA 的演示项目，实现了用户认证、会话管理和角色权限控制。支持多环境配置（dev、test、prod），并集成了 Redis 用于并发会话控制和 JWT 认证。

## 特性
- 用户注册、登录、登出和 token 刷新
- 基于 Redis 的并发会话控制（最大会话数限制）
- 角色和权限管理
- 多环境支持（开发、测试、生产）
- API 文档使用 Springdoc OpenAPI（Swagger UI）
- 单元测试和集成测试

## 技术栈
- Spring Boot 3.x
- Spring Data JPA
- MySQL / H2 数据库
- Redis
- JWT
- Lombok
- Spring Security

## 环境要求
- Java 17+
- Maven 3.x
- MySQL 8.x (生产环境)
- Redis 6.x+

## 安装和运行

### 1. 克隆仓库
```bash
git clone <repository-url>
cd backend
```

### 2. 配置环境
- **开发环境 (dev)**: 使用 H2 内存数据库，默认激活。
- **测试环境 (test)**: 使用 H2 数据库。
- **生产环境 (prod)**: 使用 MySQL，需要设置环境变量 DB_URL, DB_USERNAME, DB_PASSWORD 等。

在 `application.yml` 中配置相应属性。

### 3. 安装依赖
```bash
./mvnw clean install
```

### 4. 运行应用
- 开发环境:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
- 生产环境:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

应用启动后，访问 http://localhost:8080/api/

### 5. API 文档
访问 http://localhost:8080/api/swagger-ui.html 查看 Swagger UI。

### 6. 测试
运行单元和集成测试:
```bash
./mvnw test
```

## 部署
- 构建 JAR:
```bash
./mvnw package
```
- 运行 JAR:
```bash
java -jar target/backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## 配置说明
- JWT 密钥和过期时间在 application.yml 中配置。
- Redis 配置包括主机、端口和密码。
- 并发会话最大数在 app.security.concurrent.max-sessions 中设置。

## 贡献
欢迎提交 Pull Request 或报告问题。

## 许可证
MIT License