# Docker 环境启动说明

本文档用于说明如何使用 Docker 一键启动 PaperHub 本地开发依赖环境。

当前 Docker 环境包含：

- MySQL 8.0.33
- Redis 7.2

## 1. 前置要求

本机需要安装：

- Docker Desktop
- Docker Compose

确认 Docker 可用：

```bash
docker --version
docker compose version
```

## 2. 配置文件

项目根目录提供以下文件：

```text
docker-compose.yml
.env.example
sql/init.sql
```

说明：

- `docker-compose.yml`：定义 MySQL 和 Redis 容器。
- `.env.example`：本地环境变量示例。
- `sql/init.sql`：MySQL 首次启动时自动执行的初始化 SQL。

如果默认配置可以满足本地开发，可以直接启动，不需要创建 `.env`。

如果需要修改端口或密码，复制一份 `.env`：

```bash
copy .env.example .env
```

然后按需修改 `.env`。

## 3. 启动环境

在项目根目录执行：

```bash
docker compose up -d
```

启动后查看容器状态：

```bash
docker compose ps
```

查看日志：

```bash
docker compose logs -f
```

## 4. 停止环境

停止容器：

```bash
docker compose down
```

停止容器并删除数据卷：

```bash
docker compose down -v
```

注意：执行 `down -v` 会删除 MySQL 和 Redis 的本地数据，下次启动会重新执行 `sql/init.sql`。

## 5. 默认连接信息

### MySQL

| 项目 | 默认值 |
| --- | --- |
| Host | `localhost` |
| Port | `3306` |
| Database | `paperhub` |
| Username | `root` |
| Password | `123456` |

后端配置示例：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/paperhub?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
```

### Redis

| 项目 | 默认值 |
| --- | --- |
| Host | `localhost` |
| Port | `6379` |
| Database | `0` |

后端配置示例：

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 0
    timeout: 3000ms
```

## 6. 常见问题

### 6.1 修改 `sql/init.sql` 后没有生效

MySQL 官方镜像只会在数据目录为空时执行 `/docker-entrypoint-initdb.d` 下的 SQL。

如果需要重新执行初始化 SQL，可以删除数据卷：

```bash
docker compose down -v
docker compose up -d
```

### 6.2 本机 3306 或 6379 端口已被占用

复制 `.env.example` 为 `.env`，修改端口：

```text
MYSQL_PORT=3307
REDIS_PORT=6380
```

如果端口改成 `3307` 或 `6380`，后端 `application.yml` 中也要同步修改连接端口。

### 6.3 如何进入容器

进入 MySQL：

```bash
docker exec -it paperhub-mysql mysql -uroot -p123456 paperhub
```

进入 Redis：

```bash
docker exec -it paperhub-redis redis-cli
```
