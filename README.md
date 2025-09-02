# Vue3 + Spring Boot 前后端分离认证模板

一个基于 **Vue3 + Element Plus + Pinia + Axios**（前端）和 **Spring Boot + Spring Security + JWT + MyBatis-Plus**（后端）实现的用户登录 / 注册 / 忘记密码 的完整前后端分离认证模板。

---

## 功能特性

- ✅ 用户注册
- ✅ 用户登录（返回 JWT Token）
- ✅ 忘记密码（修改用户密码）
- ✅ JWT 鉴权（后端自动校验 Token 合法性）
- ✅ 基于 MyBatis-Plus 的 CRUD 操作
- ✅ 使用 Element Plus 美化前端 UI
- ✅ 前后端 CORS 跨域支持
- ✅ 前端基于 Pinia 管理全局状态（存储 Token）

---

## 技术栈

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
vue-spring-auth-template
├── backend # Spring Boot 后端
│ ├── src/main/java/com/myproject
│ │ ├── config # JWT & Security 配置
│ │ ├── controller # 控制层
│ │ ├── entity # 实体类
│ │ ├── mapper # MyBatis-Plus Mapper
│ │ ├── service # 业务逻辑层
│ │ └── util # 工具类 (JWT工具等)
│ └── resources
│ └── application.yml # 配置文件
│
├── frontend # Vue3 前端
│ ├── src
│ │ ├── api # axios 封装 & 接口请求
│ │ ├── store # pinia store
│ │ ├── views # 页面（登录/注册/忘记密码/首页）
│ │ └── router # vue-router 配置
│ └── vite.config.js # Vite 配置
└── README.md
```
---

## ⚡ 快速开始

### 后端运行

1. 修改 `backend/src/main/resources/application.yml` 数据库配置：
   ```
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/auth_template?useSSL=false&serverTimezone=UTC
       username: root
       password: your_password
   ```
3. 运行项目中给的sql文件

### 前端运行
1. 安装依赖
   ```
   cd frontend
   npm install
   ```
2. 运行前端
   ```
   npm run dev
   ```

   
