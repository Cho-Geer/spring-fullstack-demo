#!/bin/bash

# 部署脚本
# 用法: ./deploy.sh [dev|test|prod]

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查参数
if [ $# -eq 0 ]; then
    print_error "请指定环境: dev, test, 或 prod"
    echo "用法: $0 [dev|test|prod]"
    exit 1
fi

ENVIRONMENT=$1

# 验证环境参数
if [[ ! "$ENVIRONMENT" =~ ^(dev|test|prod)$ ]]; then
    print_error "无效的环境: $ENVIRONMENT"
    echo "支持的环境: dev, test, prod"
    exit 1
fi

print_info "开始部署到 $ENVIRONMENT 环境..."

# 检查必要的工具
check_requirements() {
    print_info "检查系统要求..."
    
    if ! command -v java &> /dev/null; then
        print_error "Java 未安装或不在 PATH 中"
        exit 1
    fi
    
    if ! command -v mvn &> /dev/null; then
        print_error "Maven 未安装或不在 PATH 中"
        exit 1
    fi
    
    print_success "系统要求检查通过"
}

# 构建应用
build_application() {
    print_info "构建应用..."
    
    # 清理并编译
    mvn clean compile
    
    # 运行测试（仅在非生产环境）
    if [ "$ENVIRONMENT" != "prod" ]; then
        print_info "运行测试..."
        mvn test
    fi
    
    # 打包
    mvn package -DskipTests
    
    print_success "应用构建完成"
}

# 部署到开发环境
deploy_dev() {
    print_info "部署到开发环境..."
    
    # 设置环境变量
    export SPRING_PROFILES_ACTIVE=dev
    
    # 启动应用
    print_info "启动开发服务器..."
    mvn spring-boot:run -Dspring-boot.run.profiles=dev &
    
    # 等待应用启动
    sleep 10
    
    # 健康检查
    if curl -f http://localhost:8080/api/test/health > /dev/null 2>&1; then
        print_success "开发环境部署成功！"
        print_info "应用访问地址: http://localhost:8080"
        print_info "H2 控制台: http://localhost:8080/h2-console"
    else
        print_error "应用启动失败，请检查日志"
        exit 1
    fi
}

# 部署到测试环境
deploy_test() {
    print_info "部署到测试环境..."
    
    # 检查Docker
    if ! command -v docker &> /dev/null; then
        print_error "Docker 未安装，测试环境需要 Docker"
        exit 1
    fi
    
    # 使用Docker Compose启动测试环境
    print_info "启动测试环境容器..."
    docker-compose --profile test up -d
    
    # 等待服务启动
    sleep 30
    
    # 健康检查
    if curl -f http://localhost:8081/api/test/health > /dev/null 2>&1; then
        print_success "测试环境部署成功！"
        print_info "应用访问地址: http://localhost:8081"
    else
        print_error "测试环境启动失败，请检查容器日志"
        docker-compose --profile test logs
        exit 1
    fi
}

# 部署到生产环境
deploy_prod() {
    print_info "部署到生产环境..."
    
    # 检查环境变量文件
    if [ ! -f ".env" ]; then
        print_warning "未找到 .env 文件，请从 .env.example 复制并配置"
        print_info "cp .env.example .env"
        print_info "然后编辑 .env 文件设置生产环境配置"
        exit 1
    fi
    
    # 加载环境变量
    source .env
    
    # 验证关键环境变量
    if [ -z "$JWT_SECRET" ] || [ "$JWT_SECRET" = "your_very_secure_jwt_secret_key_at_least_256_bits_long_abcdefghijklmnopqrstuvwxyz" ]; then
        print_error "请在 .env 文件中设置安全的 JWT_SECRET"
        exit 1
    fi
    
    if [ -z "$DB_PASSWORD" ] || [ "$DB_PASSWORD" = "your_secure_database_password" ]; then
        print_error "请在 .env 文件中设置安全的数据库密码"
        exit 1
    fi
    
    # 检查Docker
    if ! command -v docker &> /dev/null; then
        print_error "Docker 未安装，生产环境需要 Docker"
        exit 1
    fi
    
    # 构建并启动生产环境
    print_info "构建生产环境镜像..."
    docker-compose build
    
    print_info "启动生产环境..."
    docker-compose up -d
    
    # 等待服务启动
    sleep 60
    
    # 健康检查
    if curl -f http://localhost:8080/api/test/health > /dev/null 2>&1; then
        print_success "生产环境部署成功！"
        print_info "应用访问地址: http://localhost:8080"
        print_info "监控地址: http://localhost:8080/api/actuator/health"
    else
        print_error "生产环境启动失败，请检查容器日志"
        docker-compose logs
        exit 1
    fi
}

# 主流程
main() {
    check_requirements
    
    case $ENVIRONMENT in
        "dev")
            build_application
            deploy_dev
            ;;
        "test")
            build_application
            deploy_test
            ;;
        "prod")
            build_application
            deploy_prod
            ;;
    esac
    
    print_success "部署完成！"
}

# 信号处理
trap 'print_error "部署被中断"; exit 1' INT TERM

# 执行主流程
main