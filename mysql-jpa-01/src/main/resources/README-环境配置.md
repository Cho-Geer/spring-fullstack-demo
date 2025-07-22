# 环境配置说明

本项目支持开发(dev)、测试(test)、生产(prod)三种环境配置。

## 配置文件结构

- `application.yml` - 主配置文件，包含所有环境的配置
- 使用Spring Boot的Profile机制实现多环境配置

## 环境切换方式

### 1. 修改配置文件（推荐用于开发）

在 `application.yml` 中修改：
```yaml
spring:
  profiles:
    active: dev  # 改为 test 或 prod
```

### 2. 启动参数

```bash
# 开发环境
java -jar app.jar --spring.profiles.active=dev

# 测试环境
java -jar app.jar --spring.profiles.active=test

# 生产环境
java -jar app.jar --spring.profiles.active=prod
```

### 3. 环境变量

```bash
export SPRING_PROFILES_ACTIVE=prod
java -jar app.jar
```

### 4. Maven启动

```bash
# 开发环境
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 测试环境
mvn spring-boot:run -Dspring-boot.run.profiles=test

# 生产环境
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### 5. IDE配置

在IDE的运行配置中设置：
- **Program arguments**: `--spring.profiles.active=dev`
- **VM options**: `-Dspring.profiles.active=dev`
- **Environment variables**: `SPRING_PROFILES_ACTIVE=dev`

## 各环境特点

### 开发环境 (dev)

- **数据库**: H2内存数据库，支持控制台访问
- **日志级别**: DEBUG，显示SQL语句
- **JWT**: 24小时过期，开发用密钥
- **并发会话**: 最多3个
- **Redis**: 本地Redis，数据库0
- **特点**: 便于开发调试，自动建表

### 测试环境 (test)

- **数据库**: H2内存数据库，持久化配置
- **日志级别**: WARN，减少日志输出
- **JWT**: 1小时过期，测试用密钥
- **并发会话**: 最多2个，超出时拒绝登录
- **Redis**: 本地Redis，数据库1
- **特点**: 模拟生产环境，用于自动化测试

### 生产环境 (prod)

- **数据库**: MySQL，连接池优化
- **日志级别**: WARN，文件日志
- **JWT**: 1小时过期，环境变量密钥
- **并发会话**: 1个，严格控制
- **Redis**: 生产Redis，支持集群
- **特点**: 高性能，高安全性，监控支持

## 生产环境环境变量

生产环境需要设置以下环境变量：

```bash
# 数据库配置
export DB_URL="jdbc:mysql://prod-db:3306/mysql_jpa_01_prod?useSSL=true&serverTimezone=UTC"
export DB_USERNAME="prod_user"
export DB_PASSWORD="your_secure_password"

# Redis配置
export REDIS_HOST="prod-redis"
export REDIS_PORT="6379"
export REDIS_PASSWORD="your_redis_password"
export REDIS_DATABASE="0"

# JWT配置
export JWT_SECRET="your_very_secure_jwt_secret_key_at_least_256_bits_long"
export JWT_EXPIRATION="3600000"  # 1小时
export JWT_REFRESH_EXPIRATION="86400000"  # 1天

# 应用配置
export MAX_SESSIONS="1"
export PREVENT_LOGIN_IF_MAX_EXCEEDED="true"

# 服务器配置
export SERVER_PORT="8080"
```

## 配置优先级

1. 命令行参数
2. 环境变量
3. application.yml中的配置
4. 默认值

## 最佳实践

1. **敏感信息**: 生产环境的密码、密钥等通过环境变量设置
2. **配置分离**: 不同环境使用不同的配置值
3. **安全性**: 生产环境使用更严格的安全配置
4. **监控**: 生产环境启用监控和健康检查
5. **日志**: 根据环境调整日志级别和输出方式

## 验证配置

启动应用后，可以通过以下方式验证当前环境：

1. 查看启动日志中的Profile信息
2. 访问 `/api/actuator/info` 查看应用信息
3. 访问 `/api/test/health` 查看健康状态

## 故障排除

1. **Profile未生效**: 检查配置文件语法和Profile名称
2. **环境变量未读取**: 确认环境变量名称和格式正确
3. **数据库连接失败**: 检查数据库配置和网络连接
4. **Redis连接失败**: 检查Redis服务状态和配置