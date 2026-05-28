# PaperHub AI 开发规范

本文件是给 Codex / AI 编程助手使用的项目专属规则。执行任何开发任务前，必须先阅读本文件，并结合当前代码和文档确认真实现状。

## 1. 项目现状

- 项目名称：PaperHub，科研文献互助与学术交流平台。
- 后端：`paperhub-server`，Maven 多模块 Java 项目。
- 后端模块：
  - `paperhub-common`：通用返回结果、业务异常。
  - `paperhub-entity`：DTO、PO、VO。
  - `paperhub-business`：Spring Boot 启动模块、Controller、Service、Mapper、配置类。
- 前端：`paperhub-web`，Vue 3 + Vite 单页应用。
- 文档目录：`docs`。
- 数据库脚本：`sql/init.sql`。
- 本地服务编排：`docker-compose.yml`，包含 MySQL 与 Redis。

## 2. 技术栈

| 类型 | 技术 |
| --- | --- |
| 后端语言 | Java 11 |
| 后端框架 | Spring Boot 2.7.18 |
| Web/API | Spring MVC REST API |
| 参数校验 | spring-boot-starter-validation |
| ORM | MyBatis-Plus 3.5.7 |
| 数据库 | MySQL 8 |
| 缓存/验证码状态 | Redis 7.x，本地使用 `StringRedisTemplate` |
| 密码加密 | Spring Security Crypto / BCrypt |
| 邮件 | Spring Mail / SMTP |
| 前端 | Vue 3.5.x |
| 构建 | Maven、npm、Vite |

如代码、POM、README 或 docs 内容不一致，以当前源码和配置为准，并在修改前指出差异。

## 3. 当前核心入口

- 后端启动入口：`paperhub-server/paperhub-business/src/main/java/org/paperhub/PaperhubBusinessApplication.java`
- 后端配置：`paperhub-server/paperhub-business/src/main/resources/application.yml`
- MyBatis-Plus 配置：`paperhub-server/paperhub-business/src/main/java/org/paperhub/config/MybatisPlusConfig.java`
- 全局异常处理：`paperhub-server/paperhub-business/src/main/java/org/paperhub/config/GlobalExceptionHandler.java`
- 认证接口：`paperhub-server/paperhub-business/src/main/java/org/paperhub/auth/controller/AuthController.java`
- 文献求助接口：`paperhub-server/paperhub-business/src/main/java/org/paperhub/literature/controller/LitRequestController.java`
- 热点接口：`paperhub-server/paperhub-business/src/main/java/org/paperhub/topic/controller/HotTopicController.java`
- 社区帖子接口：`paperhub-server/paperhub-business/src/main/java/org/paperhub/group/controller/PostController.java`
- 前端入口：`paperhub-web/src/main.js`
- 前端主页面：`paperhub-web/src/App.vue`
- 前端样式：`paperhub-web/src/style.css`
- Vite 配置：`paperhub-web/vite.config.js`

## 4. 必须遵守的开发流程

每次收到需求后，必须按以下流程执行：

1. 先复述并确认需求理解。
2. 分析影响范围，包括后端、前端、数据库、配置、文档、测试。
3. 输出简洁需求文档内容。
4. 输出简洁技术文档内容。
5. 输出任务列表。
6. 等用户确认后，再开始修改代码。
7. 修改完成后进行接口测试或功能验证。
8. 总结修改内容、测试结果、影响范围。

如果需求存在关键歧义，且不同理解会导致明显不同方案、返工成本高或错误实现，必须先提出关键问题，不允许直接猜测实现。若只有轻微不确定，可以基于最合理假设继续，但必须明确说明假设。

## 5. 文档规范

每次新增功能或重要修改前，必须在 `docs` 目录创建三类文档：

- 需求文档
- 技术文档
- 任务列表

文档要求：

- 使用中文命名。
- 文件名必须带日期。
- 日期格式使用 `YYYY-MM-DD`。
- 文件名格式：`docs/YYYY-MM-DD-功能名称需求文档.md`、`docs/YYYY-MM-DD-功能名称技术文档.md`、`docs/YYYY-MM-DD-功能名称任务列表.md`。
- 内容简洁，只写关键需求、关键设计、关键任务。
- 禁止写成冗长说明书。

示例：

- `docs/2026-05-27-用户登录功能需求文档.md`
- `docs/2026-05-27-用户登录功能技术文档.md`
- `docs/2026-05-27-用户登录功能任务列表.md`

## 6. README 更新规范

每次新增功能后，必须在根目录 `README.md` 追加简洁说明，包括：

- 新增功能名称
- 主要改动
- 如何启动或测试
- 相关接口或页面
- 注意事项

README 只能写关键内容，禁止写长篇背景介绍。若前端子项目 `paperhub-web/README.md` 受影响，也要同步保持简洁更新。

## 7. 后端开发规范

- 新业务接口按现有包结构放入对应业务包：`auth`、`literature`、`topic`、`group`，新增领域时再创建清晰业务包。
- Controller 只做参数接收、鉴权上下文提取和返回封装，不写复杂业务逻辑。
- Service 负责业务规则和事务边界，涉及多表写入必须评估是否需要 `@Transactional(rollbackFor = Exception.class)`。
- Mapper 使用 MyBatis-Plus，优先使用 `LambdaQueryWrapper`，避免手写拼接 SQL。
- DTO/VO/PO 放在 `paperhub-entity` 对应业务包下，命名必须清晰。
- API 返回统一使用 `Result<T>`。
- 业务异常统一抛出 `BizException`，由 `GlobalExceptionHandler` 处理。
- 参数校验优先使用 `javax.validation` 注解，并补充必要的业务校验。
- 密码必须使用 BCrypt，不允许明文存储。
- Redis Key 必须集中管理，沿用 `AuthRedisKeys` 这类常量类模式。
- 配置项必须放入 `application.yml`、环境变量或配置属性类，不允许在业务代码中硬编码端口、密钥、账号、路径。

## 8. 前端开发规范

- 当前前端是 Vue 3 + Vite，主要逻辑集中在 `paperhub-web/src/App.vue`。
- 修改前先理解现有状态变量、接口调用、弹窗和页面模式，不要盲目拆分或大重构。
- 新增接口调用统一经过现有 `readApiData` 风格处理返回结果。
- 前端请求优先使用 `/api` 代理到后端，代理配置在 `paperhub-web/vite.config.js`。
- 新增表单必须包含必要的前端校验、loading 状态、错误提示和成功后的数据刷新。
- 不随意改变现有视觉风格、导航结构和页面状态流。
- 涉及上传、富文本、外链展示时，必须评估文件类型、大小、XSS 和后端接口能力。

## 9. 数据库与配置规范

- 数据库结构以 `sql/init.sql` 为初始化基准，涉及表结构变更时必须同步更新该文件和 `docs/database.md`。
- 新增字段必须确认 Java PO、数据库字段、接口文档、前端展示是否一致。
- 本地默认外部服务：MySQL `3306`、Redis `6379`、后端 `8080`、前端 Vite 默认端口。
- 真实数据库密码、SMTP 授权码、Token、API Key 不得提交。
- `application.yml` 中发现敏感信息时，必须先提醒用户，并建议改为环境变量或本地私有配置。
- `.env` 不得提交；只能提交 `.env.example` 这类示例文件。

## 10. 注释规范

所有新增或修改代码都必须添加必要注释，重点解释“为什么这样做”和关键逻辑。

需要关键注释的位置：

- 配置类
- 工具类
- Controller / API 层
- Service 层
- Repository / Mapper 层
- DTO / VO / Entity
- 中间件 / 拦截器
- 定时任务
- 异步任务
- 复杂判断逻辑
- 第三方 API 调用
- 数据库操作
- 前端组件关键状态和事件处理

注释要求：

- 解释业务原因、边界条件、关键流程。
- 不写废话注释。
- 不要每一行都机械注释。
- 已存在乱码或编码异常时，先说明风险，再谨慎修复。

## 11. 接口测试与功能验证规范

每次实现后必须验证：

- 后端接口要给出测试方式。
- 能用 curl、Postman、Swagger 测试的，必须给出示例。
- 前端功能要说明页面路径和验证步骤。
- 涉及数据库写入时，要说明需要的 MySQL/Redis 前置条件。
- 如果无法测试，必须说明原因。
- 如果测试失败，必须分析原因并修复，不能直接结束。

常用命令：

```bash
cd paperhub-server
mvn clean package -DskipTests
```

```bash
cd paperhub-web
npm install
npm run build
```

后端本地启动：

```bash
cd paperhub-server/paperhub-business
mvn spring-boot:run
```

前端本地启动：

```bash
cd paperhub-web
npm run dev
```

基础接口验证示例：

```bash
curl http://localhost:8080/api/auth/captcha
```

```bash
curl "http://localhost:8080/api/lit-request/page?current=1&size=5"
```

## 12. 安全规范

必须避免：

- 提交真实 API Key
- 提交数据库密码
- 提交 SMTP 授权码
- 提交 Token
- 提交 `.env`
- 写死账号密码
- SQL 注入风险
- XSS 风险
- 文件上传风险
- 越权访问风险

安全检查要求：

- 修改认证、用户信息、积分、文件上传、帖子内容、外链头像时必须额外检查越权、注入和 XSS。
- 后端不能只信任前端传入的 `userId`，涉及用户身份的接口应优先使用 token 或可靠鉴权上下文。
- 文件上传功能未完整接入后端前，不得假装已完成。
- 发现敏感配置或明显安全风险，必须在修改前提醒用户。

## 13. 代码边界

- 不随意移动文件。
- 不随意重构无关代码。
- 不修改与需求无关的功能。
- 不破坏现有接口路径，除非用户明确要求并同步更新前端和文档。
- 不写重复代码，必要时抽取小而清晰的工具方法。
- 不引入新框架或新中间件，除非先说明原因并获得确认。
- 不把 docs 写成大量模板文本。
- 不将 `READMD.md` 当作主 README；主文档以根目录 `README.md` 为准。

## 14. 输出规范

每次回复必须简洁清晰，优先使用：

- 简洁标题
- 简短列表
- 表格
- 明确结论

开发完成后的总结必须包含：

- 修改了哪些文件
- 完成了哪些功能
- 执行了哪些测试或验证
- 是否影响数据库、配置、接口、前端页面
- 是否存在未完成事项或风险

