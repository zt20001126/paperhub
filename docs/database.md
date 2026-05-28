# PaperHub 数据库设计

数据库初始化脚本位于：

```text
sql/init.sql
```

验证码和登录 token 当前存储在后端内存中，不创建数据库表：

- 图片验证码：`CaptchaService` 内存 Map
- 邮箱验证码：`EmailCodeService` 内存 Map
- 登录 token：`AuthServiceImpl` 内存 Map

服务重启后，上述内存数据会失效。

## paperhub_user

对应 Java 类：`SysUser`

| Java 字段 | 数据库字段 | Java 类型 | SQL 类型 | 必填 | 约束/默认值 | 说明 |
| --- | --- | --- | --- | --- | --- | --- |
| `id` | `id` | `Long` | `BIGINT` | 是 | 主键，自增 | 用户 ID |
| `email` | `email` | `String` | `VARCHAR(255)` | 是 | 唯一索引 | 登录邮箱 |
| `password` | `password` | `String` | `VARCHAR(255)` | 是 | 无 | BCrypt 加密后的密码 |
| `nickname` | `nickname` | `String` | `VARCHAR(100)` | 否 | 默认空 | 用户昵称 |
| `avatarUrl` | `avatar_url` | `String` | `TEXT` | 否 | 默认空 | 头像地址或 Base64 图片 |
| `status` | `status` | `Integer` | `INT` | 是 | 默认 `1` | 用户状态，`1` 启用，`0` 禁用 |
| `points` | `points` | `Integer` | `INT` | 是 | 默认 `100` | 用户积分 |
| `createTime` | `create_time` | `LocalDateTime` | `DATETIME` | 是 | 默认当前时间 | 创建时间 |
| `updateTime` | `update_time` | `LocalDateTime` | `DATETIME` | 是 | 自动更新时间 | 更新时间 |

## literature_request

对应 Java 类：`LitRequest`

| Java 字段 | 数据库字段 | Java 类型 | SQL 类型 | 必填 | 约束/默认值 | 说明 |
| --- | --- | --- | --- | --- | --- | --- |
| `id` | `id` | `Long` | `BIGINT` | 是 | 主键，自增 | 文献求助 ID |
| `userId` | `user_id` | `Long` | `BIGINT` | 是 | 普通索引 | 发布用户 ID |
| `title` | `title` | `String` | `VARCHAR(500)` | 是 | 无 | 论文标题 |
| `journal` | `journal` | `String` | `VARCHAR(255)` | 是 | 无 | 发表期刊 |
| `doi` | `doi` | `String` | `VARCHAR(255)` | 是 | 无 | DOI |
| `rewardPoints` | `reward_points` | `Integer` | `INT` | 是 | 默认 `0` | 奖励积分 |
| `status` | `status` | `Integer` | `INT` | 是 | 默认 `0` | 状态，`0` 待处理，`1` 处理中，`2` 已完成 |
| `createTime` | `create_time` | `LocalDateTime` | `DATETIME` | 是 | 默认当前时间 | 创建时间 |
| `updateTime` | `update_time` | `LocalDateTime` | `DATETIME` | 是 | 自动更新时间 | 更新时间 |

## literature_assist

对应 Java 类：`LitAssist`

| Java 字段 | 数据库字段 | Java 类型 | SQL 类型 | 必填 | 约束/默认值 | 说明 |
| --- | --- | --- | --- | --- | --- | --- |
| `id` | `id` | `Long` | `BIGINT` | 是 | 主键，自增 | 应助记录 ID |
| `litRequestId` | `lit_request_id` | `Long` | `BIGINT` | 是 | 普通索引 | 文献求助 ID |
| `userId` | `user_id` | `Long` | `BIGINT` | 是 | 普通索引 | 应助用户 ID |
| `originalFilename` | `original_filename` | `String` | `VARCHAR(255)` | 是 | 无 | 原始 PDF 文件名 |
| `objectName` | `object_name` | `String` | `VARCHAR(500)` | 是 | 无 | MinIO 对象名称 |
| `fileSize` | `file_size` | `Long` | `BIGINT` | 是 | 默认 `0` | 文件大小，单位字节 |
| `status` | `status` | `Integer` | `INT` | 是 | 默认 `0` | 状态，`0` 已提交，`1` 已采纳，`2` 已拒绝 |
| `createTime` | `create_time` | `LocalDateTime` | `DATETIME` | 是 | 默认当前时间 | 创建时间 |
| `updateTime` | `update_time` | `LocalDateTime` | `DATETIME` | 是 | 自动更新时间 | 更新时间 |

## post

对应 Java 类：`Post`

| Java 字段 | 数据库字段 | Java 类型 | SQL 类型 | 必填 | 约束/默认值 | 说明 |
| --- | --- | --- | --- | --- | --- | --- |
| `id` | `id` | `Long` | `BIGINT` | 是 | 主键，自增 | 帖子 ID |
| `userId` | `user_id` | `Long` | `BIGINT` | 是 | 普通索引 | 发布用户 ID |
| `title` | `title` | `String` | `VARCHAR(200)` | 是 | 无 | 帖子标题 |
| `content` | `content` | `String` | `TEXT` | 是 | 无 | 帖子内容 |
| `viewCount` | `view_count` | `Integer` | `INT` | 是 | 默认 `0` | 浏览数 |
| `likeCount` | `like_count` | `Integer` | `INT` | 是 | 默认 `0` | 点赞数 |
| `publishTime` | `publish_time` | `LocalDateTime` | `DATETIME` | 是 | 默认当前时间 | 发布时间 |
| `updateTime` | `update_time` | `LocalDateTime` | `DATETIME` | 是 | 自动更新时间 | 更新时间 |
| `status` | `status` | `Integer` | `INT` | 是 | 默认 `1` | 状态，`1` 正常，`0` 隐藏 |

## hot_topic

对应 Java 类：`HotTopic`

| Java 字段 | 数据库字段 | Java 类型 | SQL 类型 | 必填 | 约束/默认值 | 说明 |
| --- | --- | --- | --- | --- | --- | --- |
| `id` | `id` | `Long` | `BIGINT` | 是 | 主键，自增 | 热点 ID |
| `title` | `title` | `String` | `VARCHAR(255)` | 是 | 无 | 热点标题 |
| `content` | `content` | `String` | `TEXT` | 否 | 默认空 | 热点内容 |
| `coverUrl` | `cover_url` | `String` | `TEXT` | 否 | 默认空 | 封面地址 |
| `viewCount` | `view_count` | `Integer` | `INT` | 是 | 默认 `0` | 浏览数 |
| `publishTime` | `publish_time` | `LocalDateTime` | `DATETIME` | 是 | 默认当前时间 | 发布时间 |
| `updateTime` | `update_time` | `LocalDateTime` | `DATETIME` | 是 | 自动更新时间 | 更新时间 |
