# Docker 部署方案

> 适用于 Windows 11 多项目部署
> 创建时间：2026-02-21

## 为什么选择 Docker？

| 优势 | 说明 |
|------|------|
| 环境隔离 | 每个项目独立运行，互不干扰 |
| 一键部署 | `docker-compose up -d` 秒级启动 |
| 跨机器迁移 | 换电脑只需复制 docker-compose.yml |
| 技术栈隔离 | Java 11/17、Node 版本互不影响 |
| 数据持久化 | volumes 保证数据不丢 |

---

## 1. 安装 Docker Desktop

```powershell
# Windows 11 安装
winget install Docker.DockerDesktop

# 安装后重启电脑，确保 WSL2 启用
wsl --update
```

---

## 2. 项目 Docker 配置

### 目录结构
```
home-inventory/
├── backend/
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile          ← 后端镜像配置
├── frontend/
│   ├── src/
│   ├── package.json
│   └── Dockerfile          ← 前端镜像配置
├── docker-compose.yml      ←  orchestration 文件
└── DEPLOY.md              ← 本文件
```

---

## 3. 配置文件

### 3.1 后端 Dockerfile

```dockerfile
# backend/Dockerfile
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 3.2 前端 Dockerfile

```dockerfile
# frontend/Dockerfile
FROM node:18-alpine AS builder

WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### 3.3 前端 Nginx 配置

```nginx
# frontend/nginx.conf
server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 3.4 主编排文件 docker-compose.yml

```yaml
version: '3.8'

services:
  # PostgreSQL 数据库
  db:
    image: postgres:16-alpine
    container_name: home-inventory-db
    environment:
      POSTGRES_DB: home_inventory
      POSTGRES_USER: ${DB_USER:-user}
      POSTGRES_PASSWORD: ${DB_PASSWORD:-password}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    restart: unless-stopped
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USER:-user} -d home_inventory"]
      interval: 10s
      timeout: 5s
      retries: 5

  # 后端服务
  backend:
    build: ./backend
    container_name: home-inventory-backend
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/home_inventory
      SPRING_DATASOURCE_USERNAME: ${DB_USER:-user}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD:-password}
      UPLOAD_PATH: /app/uploads
    volumes:
      - uploads:/app/uploads
    ports:
      - "8080:8080"
    depends_on:
      db:
        condition: service_healthy
    restart: unless-stopped

  # 前端服务
  frontend:
    build: ./frontend
    container_name: home-inventory-frontend
    ports:
      - "80:80"
    depends_on:
      - backend
    restart: unless-stopped

volumes:
  postgres_data:    # 数据库持久化
  uploads:          # 上传文件持久化
```

---

## 4. 部署步骤

### 4.1 首次部署

```powershell
# 1. 进入项目目录
cd C:\path\to\home-inventory

# 2. 创建环境文件（可选）
@"
DB_USER=your_username
DB_PASSWORD=your_strong_password
"@ | Out-File -Encoding UTF8 .env

# 3. 构建并启动
docker-compose up -d --build

# 4. 查看状态
docker-compose ps

# 5. 查看日志
docker-compose logs -f
```

### 4.2 常用命令

```powershell
# 启动服务
docker-compose up -d

# 停止服务
docker-compose down

# 停止并删除数据（慎用）
docker-compose down -v

# 重启单个服务
docker-compose restart backend

# 查看日志
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f db

# 更新部署（代码更新后）
docker-compose down
docker-compose up -d --build

# 进入容器调试
docker exec -it home-inventory-backend sh
docker exec -it home-inventory-db psql -U user -d home_inventory
```

---

## 5. 数据备份与恢复

### 备份
```powershell
# 备份数据库
docker exec home-inventory-db pg_dump -U user home_inventory > backup_$(Get-Date -Format "yyyyMMdd").sql

# 备份上传文件
docker run --rm -v home-inventory_uploads:/data -v ${PWD}:/backup alpine tar czf /backup/uploads_$(Get-Date -Format "yyyyMMdd").tar.gz -C /data .
```

### 恢复
```powershell
# 恢复数据库
cat backup_20260221.sql | docker exec -i home-inventory-db psql -U user -d home_inventory

# 恢复上传文件
docker run --rm -v home-inventory_uploads:/data -v ${PWD}:/backup alpine tar xzf /backup/uploads_20260221.tar.gz -C /data
```

---

## 6. 多项目管理建议

```
D:\DockerProjects\
├── home-inventory\
│   ├── docker-compose.yml
│   └── ...
├── project-2\
│   ├── docker-compose.yml
│   └── ...
├── project-3\
│   ├── docker-compose.yml
│   └── ...
└── nginx-proxy\          ← 反向代理（可选）
    ├── docker-compose.yml
    └── nginx.conf
```

### 端口规划
| 项目 | 前端端口 | 后端端口 | 数据库端口 |
|------|---------|---------|-----------|
| home-inventory | 8081 | 8080 | 5432 |
| project-2 | 8082 | 8083 | 5433 |
| project-3 | 8084 | 8085 | 5434 |

---

## 7. 故障排查

### 问题：端口被占用
```powershell
# 查看占用端口的进程
netstat -ano | findstr :8080

# 杀死进程
taskkill /PID <进程ID> /F
```

### 问题：容器启动失败
```powershell
# 查看详细日志
docker-compose logs --tail=100 backend

# 检查容器状态
docker ps -a
docker inspect home-inventory-backend
```

### 问题：数据库连接失败
```powershell
# 检查数据库是否健康
docker-compose ps

# 手动测试连接
docker exec -it home-inventory-db psql -U user -d home_inventory -c "SELECT 1"
```

---

## 8. 生产环境优化

### 使用 .env 文件管理配置
```
# .env
DB_USER=prod_user
DB_PASSWORD=your_strong_password_here
DB_NAME=home_inventory
BACKEND_PORT=8080
FRONTEND_PORT=80
```

### 启用 HTTPS（使用 Let's Encrypt）
```yaml
# 添加反向代理服务
  nginx-proxy:
    image: nginxproxy/nginx-proxy
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - /var/run/docker.sock:/tmp/docker.sock:ro
      - certs:/etc/nginx/certs
```

---

## 参考资源

- [Docker 官方文档](https://docs.docker.com/)
- [Docker Compose 文档](https://docs.docker.com/compose/)
- [PostgreSQL Docker 镜像](https://hub.docker.com/_/postgres)
- [Spring Boot Docker 指南](https://spring.io/guides/topicals/spring-boot-docker/)

---

**注意**：部署前请确保已安装 Docker Desktop 并启用 WSL2 后端。
