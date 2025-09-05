
<div align="center">
  <img src="docs/AI分析.png

# NutriTrack - 营养追踪与健康管理平台

一个基于 **Vue3 + Element Plus + Pinia + Axios**（前端）和 **Spring Boot + Spring Security + JWT + MyBatis-Plus**（后端）实现的营养追踪与健康管理平台。

---

## 🌟 项目简介

NutriTrack 是一款专注于个人饮食记录与健康管理的应用，用户可以记录每日三餐情况，并选择是否分享到社区。平台提供 AI 分析功能，评估饮食健康度，并支持社区互动功能。

### 核心功能

- ✅ 用户注册与登录（JWT Token 认证）
- ✅ 忘记密码与密码重置
- ✅ 个人饮食记录管理（文字+图片）
- ✅ 社区分享与互动（点赞、评论）
- ✅ AI 饮食健康分析与建议
- ✅ 个人仪表盘概览
- ✅ 通知提醒系统

---

## 🚀 功能模块

### 1. 用户认证系统
完整的用户注册、登录、忘记密码流程，采用 JWT 进行身份验证，保障系统安全。

### 2. 个人饮食记录
- 记录每日三餐（早餐、午餐、晚餐）
- 支持文字描述与图片上传
- 可选择是否分享到社区
- 个人记录分类查看与管理

### 3. AI 健康分析
- 基于用户饮食记录提供健康评分
- 生成营养分析报告
- 提供个性化饮食改进建议
- AI 营养师助手对话功能

### 4. 社区互动
- 查看其他用户分享的饮食记录
- 点赞与评论互动
- 社区健康排行榜
- 图片预览功能

### 5. 个人仪表盘
- 统计近期饮食记录情况
- 显示健康评分趋势
- 重要通知与提醒
- 快捷操作入口

### 6. 通知系统
- 系统通知
- 社区互动提醒
- 用餐提醒功能

---

## 🛠 技术栈

### 前端
- [Vue3](https://vuejs.org/) - 渐进式 JavaScript 框架
- [Vue Router](https://router.vuejs.org/) - 路由管理
- [Pinia](https://pinia.vuejs.org/) - 状态管理
- [Element Plus](https://element-plus.org/) - UI 组件库
- [Axios](https://axios-http.com/) - HTTP 请求库
- [Vite](https://vitejs.dev/) - 前端构建工具

### 后端
- [Spring Boot 3](https://spring.io/projects/spring-boot) - 后端框架
- [Spring Security](https://spring.io/projects/spring-security) - 安全认证
- [JWT (jjwt)](https://github.com/jwtk/jjwt) - Token 鉴权
- [MyBatis-Plus](https://baomidou.com/) - ORM 框架
- [MySQL](https://www.mysql.com/) - 数据库

---

## 📂 项目结构

```
NutriTrack
├── backend # Spring Boot 后端 
│ ├── src/main/java/com/myproject 
│ │ ├── config # JWT & Security 配置 
│ │ ├── controller # 控制层 
│ │ ├── dto # 数据传输对象 
│ │ ├── entity # 实体类 
│ │ ├── enums # 枚举类型 
│ │ ├── exception # 异常处理 
│ │ ├── mapper # MyBatis-Plus Mapper 
│ │ ├── result # 统一响应结果 
│ │ ├── service # 业务逻辑层 
│ │ ├── utils # 工具类 (JWT工具等) 
│ │ └── vo # 视图对象 
│ └── resources 
│ └── application.yml # 配置文件 
├── frontend # Vue3 前端 
│ ├── src 
│ │ ├── api # axios 封装 & 接口请求 
│ │ ├── store # pinia store 
│ │ ├── views # 页面组件 
│ │ │ ├── AIAnalysis.vue # AI分析页面 
│ │ │ ├── Community.vue # 社区页面 
│ │ │ ├── Dashboard.vue # 仪表盘页面 
│ │ │ ├── MyMeals.vue # 个人饮食记录页面 
│ │ │ ├── Login.vue # 登录页面 
│ │ │ ├── Register.vue # 注册页面 
│ │ │ └── ... # 其他页面 
│ │ ├── router # vue-router 配置 
│ │ └── utils # 工具类 
│ └── vite.config.js # Vite 配置 
├── datasource # 数据库和资源文件
│ ├── NutriTrack.sql # 数据库初始化脚本
│ └── images # 示例图片资源（需要复制到 D:/NutriTrack/images/）
└── README.md
```
----

## 🗄 数据库设计

项目包含以下核心数据表：

- `users` - 用户信息表
- `meal_records` - 饮食记录表
- `meal_images` - 饮食图片表
- `daily_diet_analysis` - 每日饮食分析表
- `record_likes` - 记录点赞表
- `record_comments` - 记录评论表
- `notifications` - 通知表

---

## 🔥 直接部署

### 环境要求

在部署NutriTrack应用之前，需要确保服务器满足以下环境要求：

- Java 21 或更高版本
- MySQL 5.7+ 或更高版本
- Redis 6.0+ 或更高版本（必须，用于验证码存储）

### 部署步骤

1. **准备数据库**
   - 在MySQL中创建数据库：`CREATE DATABASE nutritrack DEFAULT CHARACTER SET utf8mb4;`
   - 运行项目中的数据库脚本：`datasource/NutriTrack.sql`

2. **准备图片存储目录**
   - 在D盘创建目录（必须一致）：`D:\NutriTrack\images\`
   - 将项目中的示例图片复制到该目录（手动复制或者执行下方指令）：
     ```bash
     # 复制 datasource/images/ 下的所有文件到 D:/NutriTrack/images/
     xcopy datasource\images\* D:\NutriTrack\images\ /E /Y
     ```

4. **启动应用**
   
   - 进入jar包所在目录
   - 运行命令：
     ```bash
     java -jar nutritrack.jar
     ```
   - 如果数据库配置与默认不同，可通过参数覆盖：
     ```bash
     java -jar nutritrack.jar --spring.datasource.username=root --spring.datasource.password=yourpassword
     ```
   
5. **访问应用**
   - 浏览器访问：http://localhost:8080

### 注意事项
- 确保 `D:\NutriTrack\images\` 目录存在且有写入权限
- 如果D盘不存在，可修改 `application.yml` 中的 `file.save-path` 为其他路径
- 首次运行会自动创建必要的数据库表
- 图片存储路径已配置为绝对路径，确保跨平台兼容性

---

## ⚡ 本地运行

### 环境准备
- Java 21
- Node.js 16+
- MySQL 5.7+
- Maven 3.6+
- Redis 6.0+（可选，用于验证码存储）

### 后端运行

1. **准备数据库**
   - 在MySQL中创建数据库：`CREATE DATABASE nutritrack DEFAULT CHARACTER SET utf8mb4;`
   - 运行数据库脚本：`datasource/NutriTrack.sql`

2. **准备图片存储目录**
   - 在D盘创建目录：`D:\NutriTrack\images\`
   - 将示例图片复制到该目录：
     ```bash
     # Windows
     xcopy datasource\images\* D:\NutriTrack\images\ /E /Y
     
     # Linux/Mac
     cp -r datasource/images/* /d/NutriTrack/images/
     ```

3. **配置数据库连接**
   - 修改 `backend/src/main/resources/application.yml` 数据库配置：
     ```yml
     spring:
       datasource:
         url: jdbc:mysql://localhost:3306/nutritrack?useSSL=true&serverTimezone=UTC&characterEncoding=utf8
         username: root
         password: your_password
       data:
         redis:
           host: 127.0.0.1
           port: 6379
           password: your_redis_password  # 如有密码请设置
     ```

4. **启动后端服务**
   ```bash 
   cd backend 
   mvn spring-boot:run
   ```

### 前端运行

1. **安装依赖**
   ```bash
   cd frontend
   npm install
   ```

2. **运行前端**
   ```bash
   npm run dev
   ```

3. **访问应用**
   - 前端开发服务器：http://localhost:5173
   - 后端API文档：http://localhost:8080/swagger-ui.html

### 开发模式说明
- 前端开发服务器会自动代理API请求到后端（8080端口）
- 图片上传会保存到 `D:\NutriTrack\images\` 目录
- 前端通过 `/images/文件名` 访问上传的图片
- 开发环境与生产环境使用相同的图片存储路径，确保一致性

---

## 📄 API 文档

后端集成 Swagger UI，可访问以下地址查看 API 文档：
- http://localhost:8080/swagger-ui.html

---

## 🤝 声明

项目为2025年南开大学软件学院暑期实训项目，详细开发过程记录可见[NKU内部飞书文档](https://nankai.feishu.cn/wiki/RGetwpj7iisN5RkxtGQc6KgTnBc)🤗🤗

------

## 🧩项目截图

<div align="center">
  <img src="docs/登录.png" alt="登录" width="50%">
  <p style="margin:5px 0;">图1 登录</p>
</div>

<div align="center">
  <img src="docs/注册.png" alt="注册" width="50%">
  <p style="margin:5px 0;">图2 注册</p>
</div>


<div align="center">
  <img src="docs/邮箱注册.png" alt="邮箱注册" width="80%">
  <p style="margin:5px 0;">图3 邮箱注册</p>
</div>
<div align="center">
  <img src="docs/重置密码.png" alt="重置密码" width="50%">
  <p style="margin:5px 0;">图4 重置密码</p>
</div>


<div align="center">
  <img src="docs/邮箱重置密码.png" alt="邮箱重置密码" width="80%">
  <p style="margin:5px 0;">图5 邮箱重置密码</p>
</div>

<div align="center">
  <img src="docs/仪表盘.png" alt="仪表盘" width="80%">
  <p style="margin:5px 0;">图6 仪表盘</p>
</div>
<div align="center">
  <img src="docs/我的饮食记录.png" alt="我的饮食记录" width="80%">
  <p style="margin:5px 0;">图7 我的饮食记录</p>
</div>

<div align="center">
  <img src="docs/添加.png" alt="添加" width="60%">
  <p style="margin:5px 0;">图8 添加饮食记录</p>
</div>


<div align="center">
  <img src="docs/社区.png" alt="社区" width="80%">
  <p style="margin:5px 0;">图9 社区</p>
</div>

<div align="center">
  <img src="docs/AI分析.png" alt="AI分析" width="80%">
  <p style="margin:5px 0;">图10 AI分析</p>
</div>

" alt="AI分析" width="80%">
  <p style="margin:5px 0;">图10 AI分析</p>
</div>
