# 🏠 Home Inventory - 家庭物品位置管理系统

一个帮助你记录和管理家中物品存放位置的 Web 应用。再也不怕找不到东西了！

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen)
![Vue](https://img.shields.io/badge/Vue-3.5-blue)
![Element Plus](https://img.shields.io/badge/Element%20Plus-2.13-blueviolet)

## ✨ 功能特性

### 核心功能
- 📦 **物品管理** - 记录物品名称、描述、图片、位置信息
- 🔍 **智能搜索** - 支持按名称、房间、位置、分类、标签搜索
- ⭐ **常用收藏** - 快速访问常用物品
- 🏠 **房间浏览** - 按房间查看所有物品
- 👀 **最近查看** - 自动记录浏览历史
- 📊 **数据统计** - 物品总数、房间分布等统计信息

### 位置管理
支持三级位置定位：
1. **房间** - 如：主卧、客厅、厨房
2. **家具/区域** - 如：衣柜、书架、抽屉
3. **具体位置** - 如：上层抽屉、第二层、左侧

## 🛠️ 技术栈

### 后端
- **Java 17** - 编程语言
- **Spring Boot 3.2** - Web 框架
- **Spring Data JPA** - 数据持久化
- **H2 Database** - 开发环境数据库（可切换 MySQL）
- **SpringDoc OpenAPI** - API 文档（Swagger UI）

### 前端
- **Vue 3** - 前端框架
- **Vite** - 构建工具
- **Element Plus** - UI 组件库
- **Vue Router** - 路由管理
- **Axios** - HTTP 客户端

## 📁 项目结构

```
home-inventory/
├── backend/                    # 后端项目
│   ├── src/main/java/         # Java 源码
│   │   └── com/homeinventory/
│   │       ├── controller/    # 控制器层
│   │       ├── service/       # 业务逻辑层
│   │       ├── repository/    # 数据访问层
│   │       ├── entity/        # 实体类
│   │       └── config/        # 配置类
│   ├── src/main/resources/    # 配置文件
│   ├── data/                  # H2 数据库文件
│   └── uploads/               # 上传的图片文件
│
└── frontend/                   # 前端项目
    ├── src/
    │   ├── components/        # 组件
    │   ├── views/             # 页面视图
    │   ├── router/            # 路由配置
    │   ├── utils/             # 工具函数
    │   └── App.vue            # 根组件
    └── vite.config.js         # Vite 配置
```

## 🚀 快速开始

### 环境要求
- Java 17+
- Node.js 18+
- Maven 3.6+

### 1. 克隆项目
```bash
git clone https://github.com/Qioooba/home-inventory.git
cd home-inventory
```

### 2. 启动后端
```bash
cd backend

# 方式一：使用 Maven 运行
./mvnw spring-boot:run

# 方式二：打包后运行
./mvnw clean package
java -jar target/home-inventory-backend-1.0.0.jar
```

后端服务启动后：
- API 地址：`http://localhost:8080`
- Swagger 文档：`http://localhost:8080/swagger-ui.html`

### 3. 启动前端
```bash
cd frontend

# 安装依赖
npm install

# 开发模式启动
npm run dev

# 构建生产版本
npm run build
```

前端服务启动后：
- 开发服务器：`http://localhost:5173`

## 📱 使用指南

### 添加物品
1. 点击底部导航栏的 "+" 按钮
2. 填写物品信息（名称、房间、位置等）
3. 上传物品照片（可选）
4. 点击保存

### 搜索物品
1. 在首页搜索框输入关键词
2. 支持快捷标签搜索（身份证、充电器、钥匙等）
3. 搜索结果按收藏优先排序

### 管理收藏
1. 在物品详情页点击 ⭐ 按钮
2. 收藏的物品会显示在首页"常用物品"区域

### 查看房间物品
1. 点击底部导航栏"房间"标签
2. 查看所有房间列表
3. 点击房间查看该房间所有物品

## 🔧 配置说明

### 后端配置
配置文件：`backend/src/main/resources/application.yml`

```yaml
# 文件上传路径
upload:
  path: ./uploads/

# H2 数据库配置（开发环境）
spring:
  datasource:
    url: jdbc:h2:file:./data/home_inventory
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

### 切换 MySQL（生产环境）
1. 在 `pom.xml` 中取消 MySQL 依赖的注释
2. 修改 `application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/home_inventory
    username: your_username
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 前端配置
配置文件：`frontend/src/utils/api.js`

```javascript
// 修改后端 API 地址
const BASE_URL = 'http://localhost:8080'
```

## 📸 截图预览

> 截图待补充

## 📝 API 文档

启动后端后访问：`http://localhost:8080/swagger-ui.html`

主要接口：
- `GET /api/items` - 获取所有物品
- `GET /api/items/{id}` - 获取物品详情
- `POST /api/items` - 创建物品
- `PUT /api/items/{id}` - 更新物品
- `DELETE /api/items/{id}` - 删除物品
- `GET /api/items/search` - 搜索物品
- `GET /api/items/stats` - 获取统计信息
- `POST /api/upload` - 上传图片

## 🗺️ 路线图

- [ ] 移动端适配优化
- [ ] 二维码标签生成
- [ ] 物品借用/归还记录
- [ ] 过期提醒功能
- [ ] 多用户支持
- [ ] 数据备份/恢复

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

MIT License

## 👤 作者

- GitHub: [@Qioooba](https://github.com/Qioooba)

---

> 💡 **提示**：建议定期备份 `backend/data/` 和 `backend/uploads/` 目录，以防数据丢失。
