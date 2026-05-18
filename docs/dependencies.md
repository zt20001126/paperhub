# PaperHub 依赖说明

PaperHub 不是 Python 项目，不需要 `requirements.txt`。后端依赖由 Maven 管理，前端依赖由 npm 管理。

## 后端运行环境

- JDK：11 或更高版本
- Maven：3.x
- MySQL：8.x
- SMTP 邮件服务：用于发送注册邮箱验证码

## 后端 Maven 依赖

依赖声明位置：

- `paperhub-server/pom.xml`
- `paperhub-server/paperhub-business/pom.xml`

主要依赖：

| 依赖 | 版本来源 | 用途 |
| --- | --- | --- |
| Spring Boot | `2.7.18` | Web 服务、依赖管理 |
| `spring-boot-starter-web` | Spring Boot BOM | REST API |
| `spring-boot-starter-validation` | Spring Boot BOM | 请求参数校验 |
| `spring-boot-starter-mail` | Spring Boot BOM | 邮箱验证码发送 |
| MyBatis-Plus | `3.5.7` | ORM 和分页 |
| MySQL Connector/J | `8.0.33` | MySQL 数据库连接 |
| `spring-security-crypto` | Spring Boot BOM | BCrypt 密码加密 |
| `spring-context-support` | Spring Boot BOM | 邮件等 Spring 扩展支持 |

后端构建：

```bash
cd paperhub-server
mvn clean package -DskipTests
```

## 前端运行环境

- Node.js
- npm

依赖声明位置：

```text
paperhub-web/package.json
```

主要依赖：

| 依赖 | 版本 | 用途 |
| --- | --- | --- |
| Vue | `^3.5.30` | 前端 UI 框架 |
| Vite | `^8.0.0` | 前端开发服务器与构建工具 |
| `@vitejs/plugin-vue` | `^6.0.5` | Vite Vue 单文件组件支持 |

前端安装与构建：

```bash
cd paperhub-web
npm install
npm run build
```

## 外部服务

### MySQL

默认后端连接：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/paperhub?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
```

### SMTP 邮件服务

注册流程会发送邮箱验证码。当前配置使用 SMTP：

```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 587
    username: 你的邮箱
    password: 你的邮箱授权码
```

注意：邮箱密码应使用授权码，不是登录密码。
