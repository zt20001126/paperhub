# PaperHub

PaperHub 是一个面向科研用户的文献互助与学术交流平台。项目包含 Spring Boot 后端和 Vue/Vite 前端，提供用户登录注册、文献求助、热点话题、社区帖子等基础功能。

## 功能介绍

### 功能名称

PaperHub 学术文献互助平台

### 功能描述

- 用户可通过邮箱注册账号，注册流程包含图片验证码、邮箱验证码、密码复杂度校验和两次密码确认。
- 用户登录后可发布文献求助，系统会扣除对应奖励积分。
- 用户可浏览文献求助列表、热点话题列表和社区帖子列表。
- 用户可发布社区帖子、修改个人昵称和头像。

### 使用场景

- 科研用户发布论文、期刊、DOI 等文献求助信息。
- 平台用户浏览并响应文献互助需求。
- 用户围绕科研主题进行社区交流。
- 管理或展示学术热点内容。

## 项目结构

```text
paperhub/
├─ paperhub-server/       后端 Maven 多模块项目
│  ├─ paperhub-common/    通用结果、业务异常
│  ├─ paperhub-entity/    DTO、Entity、VO
│  └─ paperhub-business/  Spring Boot 启动模块和业务实现
├─ paperhub-web/          前端 Vue 3 + Vite 项目
├─ docs/                  接口、数据库、依赖文档
└─ sql/                   数据库初始化 SQL
```

## 功能实现文件

### 后端

- `paperhub-server/paperhub-business/src/main/java/org/paperhub/PaperhubBusinessApplication.java`：后端启动入口。
- `paperhub-server/paperhub-business/src/main/java/org/paperhub/auth/controller/AuthController.java`：认证接口。
- `paperhub-server/paperhub-business/src/main/java/org/paperhub/literature/controller/LitRequestController.java`：文献求助接口。
- `paperhub-server/paperhub-business/src/main/java/org/paperhub/topic/controller/HotTopicController.java`：热点话题接口。
- `paperhub-server/paperhub-business/src/main/java/org/paperhub/group/controller/PostController.java`：社区帖子接口。
- `paperhub-server/paperhub-business/src/main/java/org/paperhub/auth/service/CaptchaService.java`：图片验证码生成与内存校验。
- `paperhub-server/paperhub-business/src/main/java/org/paperhub/auth/service/EmailCodeService.java`：邮箱验证码发送与内存校验。

### 前端

- `paperhub-web/src/main.js`：前端入口。
- `paperhub-web/src/App.vue`：主页面、登录注册弹窗、文献求助、热点和社区交互。
- `paperhub-web/vite.config.js`：Vite 配置，开发环境将 `/api` 代理到后端 `8080` 端口。

## 环境依赖

项目不是 Python 项目，不需要 `requirements.txt`。

- 后端依赖以 Maven 为准：`paperhub-server/pom.xml` 和 `paperhub-server/paperhub-business/pom.xml`
- 前端依赖以 npm 为准：`paperhub-web/package.json`
- 详细依赖说明见：`docs/dependencies.md`

## 环境要求

- JDK 11 或更高版本
- Maven 3.x
- Node.js 和 npm
- MySQL 8
- 可用 SMTP 邮件服务，例如 QQ 邮箱 SMTP 授权码

## 数据库初始化

初始化脚本：

```text
sql/init.sql
```

执行方式示例：

```bash
mysql -uroot -p < sql/init.sql
```

如果使用 Docker MySQL：

```bash
docker exec -i paperhub-mysql mysql -uroot -p123456 < sql/init.sql
```

## 启动方式

### 后端

后端默认端口为 `8080`。

```bash
cd paperhub-server
mvn clean package -DskipTests
cd paperhub-business
mvn spring-boot:run
```

也可以使用 jar 启动：

```bash
cd paperhub-server/paperhub-business
java -jar target/paperhub-business-1.0-SNAPSHOT.jar
```

### 前端

```bash
cd paperhub-web
npm install
npm run dev
```

生产构建：

```bash
npm run build
```

## 配置说明

后端配置文件：

```text
paperhub-server/paperhub-business/src/main/resources/application.yml
```

主要配置项：

- `spring.datasource.url`：MySQL 连接地址
- `spring.datasource.username`：MySQL 用户名
- `spring.datasource.password`：MySQL 密码
- `spring.mail.host`：SMTP 服务器
- `spring.mail.port`：SMTP 端口
- `spring.mail.username`：发件邮箱
- `spring.mail.password`：邮箱授权码

建议不要把真实邮箱授权码提交到公开仓库。

## 简单使用示例

### 1. 获取图片验证码

```bash
curl http://localhost:8080/api/auth/captcha
```

### 2. 发送邮箱验证码

```bash
curl -X POST http://localhost:8080/api/auth/send-code \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"user@example.com\",\"captchaId\":\"captcha-id\",\"captchaCode\":\"ABCD\"}"
```

### 3. 注册

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"user@example.com\",\"emailCode\":\"123456\",\"password\":\"abc12345\",\"confirmPassword\":\"abc12345\",\"captchaId\":\"captcha-id\",\"captchaCode\":\"ABCD\"}"
```

### 4. 登录

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"user@example.com\",\"password\":\"abc12345\"}"
```

## 更多文档

- 接口文档：`docs/api.md`
- 数据库设计：`docs/database.md`
- 依赖说明：`docs/dependencies.md`

## 当前限制

- 登录 token、邮箱验证码、图片验证码当前都存储在后端内存中，服务重启后会失效。
- 前端存在 `/sub_lit_help` 文件上传预留调用，但后端当前没有对应接口。
