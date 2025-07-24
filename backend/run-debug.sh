#!/bin/bash

# 设置环境变量
export SPRING_PROFILES_ACTIVE=dev

# 清理日志文件
rm -f logs/application.log

# 使用Maven启动应用
mvn spring-boot:run