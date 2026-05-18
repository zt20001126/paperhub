# PaperHub 项目说明

PaperHub 是一个文献互助与学术交流平台，项目由后端 Spring Boot 服务和前端 Vue/Vite 单页应用组成。

## 项目结构

```text
paperhub/
├─ paperhub-server/       后端 Maven 多模块项目
│  ├─ paperhub-common/    通用返回结果、业务异常等公共代码
│  ├─ paperhub-entity/    DTO、Entity、VO
│  └─ paperhub-business/  Spring Boot 启动模块、Controller、Service、Mapper
└─ paperhub-web/          前端 Vue 3 + Vite 项目
```

## 技术栈

- 后端：Java、Spring Boot 2.7.18、MyBatis-Plus 3.5.7、MySQL 8
- 前端：Vue 3、Vite
- 构建工具：Maven、npm
- 外部服务：MySQL、SMTP 邮件服务

## 环境要求

- JDK 11 或更高版本
- Maven 3.x
- Node.js 和 npm
- Docker，用于启动 MySQL 服务

当前后端 POM 中配置的 Java 编译目标是 11：

```xml
<java.version>11</java.version>
```

## Docker 启动 MySQL

项目后端默认连接本机 MySQL：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/paperhub?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
```

可以使用 Docker 启动一个与默认配置匹配的 MySQL 8 容器：

```bash
docker run -d \
  --name paperhub-mysql \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=123456 \
  -e MYSQL_DATABASE=paperhub \
  -e TZ=Asia/Shanghai \
  -v paperhub-mysql-data:/var/lib/mysql \
  mysql:8.0 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci
```

Windows PowerShell 可使用单行命令：

```powershell
docker run -d --name paperhub-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 -e MYSQL_DATABASE=paperhub -e TZ=Asia/Shanghai -v paperhub-mysql-data:/var/lib/mysql mysql:8.0 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
```

如果容器已经存在，可使用：

```bash
docker start paperhub-mysql
```

## 数据库初始化

项目中没有发现现成的 SQL 初始化脚本，需要先创建业务表。进入 MySQL 后执行下面 SQL：

```sql
CREATE DATABASE IF NOT EXISTS paperhub
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE paperhub;

CREATE TABLE IF NOT EXISTS paperhub_user (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  nickname VARCHAR(100) DEFAULT NULL,
  avatar_url TEXT DEFAULT NULL,
  status INT DEFAULT 1,
  points INT DEFAULT 100,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS literature_request (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  title VARCHAR(500) NOT NULL,
  journal VARCHAR(255) NOT NULL,
  doi VARCHAR(255) NOT NULL,
  reward_points INT DEFAULT 0,
  status INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_literature_request_user_id (user_id),
  INDEX idx_literature_request_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS post (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  content TEXT NOT NULL,
  view_count INT DEFAULT 0,
  like_count INT DEFAULT 0,
  publish_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  status INT DEFAULT 1,
  INDEX idx_post_user_id (user_id),
  INDEX idx_post_publish_time (publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS hot_topic (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  content TEXT DEFAULT NULL,
  cover_url TEXT DEFAULT NULL,
  view_count INT DEFAULT 0,
  publish_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_hot_topic_view_count (view_count),
  INDEX idx_hot_topic_publish_time (publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

使用 Docker 容器执行 SQL 的方式：

```bash
docker exec -it paperhub-mysql mysql -uroot -p123456
```

然后粘贴上面的 SQL。

## 邮件配置

注册流程会调用 SMTP 发送邮箱验证码，配置位置：

```text
paperhub-server/paperhub-business/src/main/resources/application.yml
```

当前配置使用 QQ 邮箱 SMTP：

```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 587
    username: 你的邮箱
    password: 你的邮箱授权码
```

注意：这里的 `password` 应使用邮箱授权码，而不是邮箱登录密码。正式开发时建议不要把真实邮箱和授权码提交到代码仓库，可以改用环境变量或本地私有配置文件。

## 后端启动方式

后端启动模块是：

```text
paperhub-server/paperhub-business/src/main/java/org/paperhub/PaperhubBusinessApplication.java
```

启动前请确保 MySQL 容器已经运行，并且数据库表已经初始化。

在项目后端目录执行：

```bash
cd paperhub-server
mvn clean package -DskipTests
```

启动方式一，使用 Spring Boot Maven 插件：

```bash
cd paperhub-server/paperhub-business
mvn spring-boot:run
```

启动方式二，使用打包后的 jar：

```bash
cd paperhub-server/paperhub-business
java -jar target/paperhub-business-1.0-SNAPSHOT.jar
```

后端默认端口：

```text
http://localhost:8080
```

## 前端启动方式

前端目录：

```bash
cd paperhub-web
```

安装依赖：

```bash
npm install
```

开发启动：

```bash
npm run dev
```

生产构建：

```bash
npm run build
```

预览构建产物：

```bash
npm run preview
```

Vite 开发服务会把 `/api` 请求代理到后端：

```js
server: {
  proxy: {
    '/api': {
      target: 'http://127.0.0.1:8080',
      changeOrigin: true
    }
  }
}
```

因此本地开发时需要同时启动：

1. Docker MySQL
2. 后端 Spring Boot 服务，端口 8080
3. 前端 Vite 服务

## 常用接口

- `POST /api/auth/send-code`：发送注册验证码
- `POST /api/auth/register`：注册
- `POST /api/auth/login`：登录
- `PUT /api/auth/update_user_info`：修改用户信息
- `GET /api/lit-request/page`：分页查询文献求助
- `POST /api/lit-request/sub_lit`：发布文献求助
- `GET /api/hot_topic_page`：查询热点列表
- `POST /api/hot_topic_view`：热点浏览量加一
- `GET /api/group/post/list`：查询社区帖子
- `POST /api/group/post/publish`：发布社区帖子

## 启动顺序建议

```bash
# 1. 启动 MySQL
docker start paperhub-mysql

# 2. 启动后端
cd paperhub-server/paperhub-business
mvn spring-boot:run

# 3. 启动前端
cd paperhub-web
npm run dev
```

## 注意事项

- 后端只配置了 MySQL 和 SMTP 邮件服务，未发现 Redis、消息队列、对象存储等其他必需服务。
- 前端代码中有一个 `/sub_lit_help` 文件上传接口，但后端当前没有对应 Controller；这部分功能属于预留或未完成接口。
- 登录 token 当前保存在后端内存 `ConcurrentHashMap` 中，服务重启后 token 会失效。
- 如果 Docker MySQL 没有暴露到本机 `3306`，需要同步修改 `application.yml` 中的 `spring.datasource.url`。
