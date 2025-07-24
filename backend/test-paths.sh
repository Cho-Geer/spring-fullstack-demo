#!/bin/bash

# 设置API基础URL
BASE_URL="http://localhost:8080"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}===== 测试不同路径的认证行为 =====${NC}"

# 测试登录接口
echo -e "\n${YELLOW}1. 测试登录接口 (POST /api/auth/login)${NC}"
curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"admin", "password":"password"}' \
  "${BASE_URL}/api/auth/login" | jq

# 保存token
TOKEN=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"admin", "password":"password"}' \
  "${BASE_URL}/api/auth/login" | jq -r '.accessToken')

echo -e "\n${GREEN}获取到的Token: ${TOKEN}${NC}"

# 测试/api/auth/路径
echo -e "\n${YELLOW}2. 测试/api/auth/路径 (GET /api/auth/test-path)${NC}"
curl -s -X GET \
  "${BASE_URL}/api/auth/test-path" | jq

# 测试/api/auth/路径（带Token）
echo -e "\n${YELLOW}3. 测试/api/auth/路径（带Token）(GET /api/auth/test-path)${NC}"
curl -s -X GET \
  -H "Authorization: Bearer ${TOKEN}" \
  "${BASE_URL}/api/auth/test-path" | jq

# 测试/api/public/路径
echo -e "\n${YELLOW}4. 测试/api/public/路径 (GET /api/public/test-path)${NC}"
curl -s -X GET \
  "${BASE_URL}/api/public/test-path" | jq

# 测试/api/protected/路径（不带Token）
echo -e "\n${YELLOW}5. 测试/api/protected/路径（不带Token）(GET /api/protected/test-path)${NC}"
curl -s -X GET \
  "${BASE_URL}/api/protected/test-path" | jq

# 测试/api/protected/路径（带Token）
echo -e "\n${YELLOW}6. 测试/api/protected/路径（带Token）(GET /api/protected/test-path)${NC}"
curl -s -X GET \
  -H "Authorization: Bearer ${TOKEN}" \
  "${BASE_URL}/api/protected/test-path" | jq

echo -e "\n${BLUE}===== 测试完成 =====${NC}"