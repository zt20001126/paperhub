# 用户登录注册功能 Task 文档

## 1. 文档说明

本文档基于以下文档拆解开发任务：

- `docs/用户登录注册功能.md`
- `docs/用户登录注册功能技术文档.md`

目标是将登录注册优化需求拆成可执行、可验收的任务项，方便后续开发、联调和测试。

## 2. 任务总览

| 任务编号 | 任务名称 | 优先级 | 状态 | 依赖 |
| --- | --- | --- | --- | --- |
| T01 | 增加 Redis 依赖和配置 | P0 | 待开发 | 无 |
| T02 | 新增认证配置类 `AuthProperties` | P0 | 待开发 | T01 |
| T03 | 新增 Redis Key 常量类 `AuthRedisKeys` | P0 | 待开发 | T01 |
| T04 | 改造图形验证码为 Redis 存储 | P0 | 待开发 | T01、T02、T03 |
| T05 | 改造邮箱验证码为 Redis 存储 | P0 | 待开发 | T01、T02、T03 |
| T06 | 增加邮箱验证码发送冷却 | P0 | 待开发 | T05 |
| T07 | 增加验证码错误次数限制 | P0 | 待开发 | T04、T05 |
| T08 | 新增登录失败限制服务 | P0 | 待开发 | T01、T02、T03 |
| T09 | 改造登录 DTO，增加图形验证码字段 | P0 | 待开发 | 无 |
| T10 | 改造登录流程，接入图形验证码和失败次数限制 | P0 | 待开发 | T04、T08、T09 |
| T11 | 梳理注册流程图形验证码校验策略 | P1 | 待开发 | T04、T05 |
| T12 | 统一异常提示和安全返回 | P1 | 待开发 | T04-T10 |
| T13 | 补充后端测试用例 | P1 | 待开发 | T04-T12 |
| T14 | 前端登录页联调 | P1 | 待开发 | T09、T10 |
| T15 | 前端注册页联调 | P1 | 待开发 | T04-T07、T11 |
| T16 | 更新项目文档和部署说明 | P2 | 待开发 | T01-T15 |

状态说明：

- 待开发：尚未开始。
- 开发中：正在实现。
- 待联调：代码完成，等待前后端联调。
- 待测试：联调完成，等待测试。
- 已完成：通过验收。

## 3. 后端基础设施任务

### T01 增加 Redis 依赖和配置

目标：

- 引入 Spring Boot Redis 能力。
- 配置 Redis 连接信息。

涉及文件：

```text
paperhub-server/paperhub-business/pom.xml
paperhub-server/paperhub-business/src/main/resources/application.yml
```

执行内容：

1. 在 `paperhub-business/pom.xml` 中增加 `spring-boot-starter-data-redis`。
2. 在配置文件中增加 Redis 连接配置。
3. 本地启动 Redis，确认后端可以正常连接。

验收标准：

- 项目可以正常编译。
- Spring 容器可以注入 `StringRedisTemplate`。
- Redis 未启动时，后端启动或接口异常表现明确。

### T02 新增认证配置类 `AuthProperties`

目标：

- 将验证码有效期、错误次数、登录锁定等配置集中管理。

涉及文件：

```text
paperhub-server/paperhub-business/src/main/java/org/paperhub/config/AuthProperties.java
paperhub-server/paperhub-business/src/main/resources/application.yml
```

执行内容：

1. 新增 `AuthProperties`。
2. 使用 `@ConfigurationProperties(prefix = "paperhub.auth")` 绑定配置。
3. 增加图形验证码、邮箱验证码、登录限制、密码配置字段。

验收标准：

- 配置项可以被正常读取。
- 业务代码不再硬编码验证码过期时间和最大错误次数。

### T03 新增 Redis Key 常量类 `AuthRedisKeys`

目标：

- 统一维护认证模块 Redis Key，避免 Key 散落在业务代码中。

涉及文件：

```text
paperhub-server/paperhub-business/src/main/java/org/paperhub/auth/constant/AuthRedisKeys.java
```

执行内容：

1. 新增 `AuthRedisKeys`。
2. 提供图形验证码、邮箱验证码、发送冷却、登录失败次数相关 Key 生成方法。
3. Key 命名与技术文档保持一致。

验收标准：

- 所有认证相关 Redis Key 均通过该类生成。
- Key 前缀清晰，包含业务场景，例如 `register`。

## 4. 验证码改造任务

### T04 改造图形验证码为 Redis 存储

目标：

- 将 `CaptchaService` 中的内存存储迁移到 Redis。

涉及文件：

```text
paperhub-server/paperhub-business/src/main/java/org/paperhub/auth/service/CaptchaService.java
```

执行内容：

1. 移除 `ConcurrentHashMap` 和 `CaptchaRecord`。
2. 注入 `StringRedisTemplate` 和 `AuthProperties`。
3. 生成验证码时写入 `auth:captcha:{captchaId}`。
4. 设置验证码 TTL。
5. 校验成功后删除 Redis Key。
6. 验证码答案统一转小写保存和比较。

验收标准：

- 获取图形验证码后，Redis 中存在对应 Key。
- Key 具备过期时间。
- 正确验证码校验成功后 Key 被删除。
- 错误或过期验证码无法通过校验。

### T05 改造邮箱验证码为 Redis 存储

目标：

- 将 `EmailCodeService` 中的内存存储迁移到 Redis。

涉及文件：

```text
paperhub-server/paperhub-business/src/main/java/org/paperhub/auth/service/EmailCodeService.java
```

执行内容：

1. 移除 `ConcurrentHashMap` 和 `CodeRecord`。
2. 注入 `StringRedisTemplate` 和 `AuthProperties`。
3. 邮件发送成功后写入 `auth:email-code:register:{email}`。
4. 设置邮箱验证码 TTL。
5. 邮箱验证码校验成功后删除 Redis Key。

验收标准：

- 邮箱验证码发送成功后 Redis 中存在对应 Key。
- Key 具备过期时间。
- 正确邮箱验证码校验成功后 Key 被删除。
- 过期或错误验证码无法注册成功。

### T06 增加邮箱验证码发送冷却

目标：

- 防止同一邮箱短时间内重复发送验证码。

涉及文件：

```text
paperhub-server/paperhub-business/src/main/java/org/paperhub/auth/service/EmailCodeService.java
```

执行内容：

1. 发送前检查 `auth:email-code:cooldown:register:{email}`。
2. 冷却 Key 存在时返回“发送过于频繁，请稍后再试”。
3. 邮件发送成功后写入冷却 Key。
4. 冷却时间使用配置项，默认 60 秒。

验收标准：

- 同一邮箱 60 秒内重复发送会被拦截。
- 冷却时间结束后可以再次发送。
- 邮件发送失败时不写入冷却 Key。

### T07 增加验证码错误次数限制

目标：

- 限制图形验证码和邮箱验证码的错误尝试次数。

涉及文件：

```text
paperhub-server/paperhub-business/src/main/java/org/paperhub/auth/service/CaptchaService.java
paperhub-server/paperhub-business/src/main/java/org/paperhub/auth/service/EmailCodeService.java
```

执行内容：

1. 图形验证码错误时递增 `auth:captcha:fail:{captchaId}`。
2. 邮箱验证码错误时递增 `auth:email-code:fail:register:{email}`。
3. 第一次递增时设置失败次数 Key 的 TTL。
4. 错误次数达到上限后删除验证码 Key 和失败次数 Key。
5. 错误次数上限使用配置项，默认 5 次。

验收标准：

- 连续输错验证码会累计失败次数。
- 达到最大错误次数后验证码失效。
- 重新获取验证码后失败次数重新计算。

## 5. 登录注册流程任务

### T08 新增登录失败限制服务

目标：

- 增加账号维度的登录失败次数限制。

涉及文件：

```text
paperhub-server/paperhub-business/src/main/java/org/paperhub/auth/service/LoginFailService.java
```

执行内容：

1. 新增 `LoginFailService`。
2. 实现 `checkLocked(String account)`。
3. 实现 `recordFail(String account)`。
4. 实现 `clearFail(String account)`。
5. 使用 `auth:login:fail:{account}` 存储失败次数。
6. 默认 5 次失败后锁定 15 分钟。

验收标准：

- 登录失败次数会写入 Redis。
- 达到最大失败次数后登录被临时锁定。
- 登录成功后失败次数被清除。

### T09 改造登录 DTO，增加图形验证码字段

目标：

- 让登录接口支持图形验证码校验参数。

涉及文件：

```text
paperhub-server/paperhub-entity/src/main/java/org/paperhub/auth/dto/LoginRequest.java
```

执行内容：

1. 新增 `captchaId` 字段。
2. 新增 `captchaCode` 字段。
3. 两个字段增加 `@NotBlank` 校验。
4. 确认前端登录请求同步传入这两个字段。

验收标准：

- 登录接口不传图形验证码参数时，参数校验失败。
- 登录接口传入参数后可以进入业务校验流程。

### T10 改造登录流程，接入图形验证码和失败次数限制

目标：

- 登录时必须校验图形验证码。
- 账号或密码错误时累计失败次数。

涉及文件：

```text
paperhub-server/paperhub-business/src/main/java/org/paperhub/auth/service/impl/AuthServiceImpl.java
```

执行内容：

1. 登录开始时调用 `loginFailService.checkLocked(email)`。
2. 查询用户和密码校验前调用 `captchaService.verify(captchaId, captchaCode)`。
3. 账号不存在或密码错误时返回统一提示“账号或密码错误”。
4. 账号不存在或密码错误时调用 `loginFailService.recordFail(email)`。
5. 登录成功后调用 `loginFailService.clearFail(email)`。
6. 登录成功后返回原有 `LoginUserVO` 结构。

验收标准：

- 登录时图形验证码必填且必须正确。
- 图形验证码正确但密码错误时登录失败次数增加。
- 连续失败达到上限后登录被锁定。
- 登录成功后失败次数清空。

### T11 梳理注册流程图形验证码校验策略

目标：

- 明确注册流程中图形验证码只校验一次，避免重复使用同一个验证码。

涉及文件：

```text
paperhub-server/paperhub-business/src/main/java/org/paperhub/auth/service/impl/AuthServiceImpl.java
paperhub-server/paperhub-entity/src/main/java/org/paperhub/auth/dto/RegisterRequest.java
```

推荐方案：

- 发送邮箱验证码接口校验图形验证码。
- 注册提交接口只校验邮箱验证码、密码和确认密码。
- `RegisterRequest` 可以移除 `captchaId`、`captchaCode`，或暂时保留但不作为注册提交的必填项。

执行内容：

1. 确认注册页交互是否接受“发送邮箱验证码时校验图形验证码”。
2. 调整 `sendRegisterCode`，图形验证码校验成功后删除验证码。
3. 调整 `register`，只校验邮箱验证码和密码。
4. 若保留兼容字段，避免前端必须重复输入图形验证码。

验收标准：

- 发送邮箱验证码前必须通过图形验证码。
- 注册提交不复用已消费的图形验证码。
- 注册成功后邮箱验证码被删除。

### T12 统一异常提示和安全返回

目标：

- 统一登录注册相关异常文案，避免暴露敏感信息。

涉及文件：

```text
paperhub-server/paperhub-business/src/main/java/org/paperhub/auth/service/impl/AuthServiceImpl.java
paperhub-server/paperhub-business/src/main/java/org/paperhub/auth/service/CaptchaService.java
paperhub-server/paperhub-business/src/main/java/org/paperhub/auth/service/EmailCodeService.java
paperhub-server/paperhub-business/src/main/java/org/paperhub/config/GlobalExceptionHandler.java
```

执行内容：

1. 登录账号不存在和密码错误统一返回“账号或密码错误”。
2. 图形验证码错误、过期、不存在统一返回“图形验证码错误或已过期”。
3. 邮箱验证码错误、过期、不存在统一返回“邮箱验证码错误或已过期”。
4. Redis 异常统一返回“系统繁忙，请稍后再试”。
5. 邮件异常统一返回“邮件发送失败，请稍后再试”。
6. 检查日志，禁止打印明文密码和验证码答案。

验收标准：

- 异常提示与 PRD 保持一致。
- 登录错误不会暴露账号是否存在。
- 日志中不出现明文密码、邮箱验证码、图形验证码答案。

## 6. 前端联调任务

### T14 前端登录页联调

目标：

- 登录页接入图形验证码。

涉及范围：

```text
paperhub-web/
```

执行内容：

1. 登录页加载时请求 `/api/auth/captcha`。
2. 保存 `captchaId`。
3. 展示 `imageBase64`。
4. 登录请求携带 `captchaId` 和 `captchaCode`。
5. 点击验证码图片可刷新。
6. 登录失败后刷新图形验证码。

验收标准：

- 登录页可以正常显示图形验证码。
- 不输入图形验证码不能登录。
- 图形验证码错误时提示并刷新。
- 登录成功流程不受影响。

### T15 前端注册页联调

目标：

- 注册页接入图形验证码、邮箱验证码和发送冷却。

涉及范围：

```text
paperhub-web/
```

执行内容：

1. 注册页加载时请求 `/api/auth/captcha`。
2. 保存 `captchaId`。
3. 发送邮箱验证码时携带邮箱、`captchaId`、`captchaCode`。
4. 发送成功后按钮进入 60 秒倒计时。
5. 发送失败时展示后端提示。
6. 注册提交时携带邮箱、邮箱验证码、密码、确认密码。
7. 密码和确认密码不一致时前端即时提示。

验收标准：

- 图形验证码错误时不能发送邮箱验证码。
- 邮箱验证码发送成功后倒计时生效。
- 注册成功后跳转登录页。
- 注册失败时展示明确提示。

## 7. 测试任务

### T13 补充后端测试用例

目标：

- 覆盖验证码 Redis 存储、错误次数、发送冷却、登录失败限制等核心逻辑。

测试范围：

```text
paperhub-server/
```

测试用例：

1. 获取图形验证码后 Redis 存在验证码 Key。
2. 图形验证码正确校验后 Key 被删除。
3. 图形验证码错误会增加失败次数。
4. 图形验证码错误次数达到上限后验证码失效。
5. 邮箱验证码发送成功后 Redis 存在邮箱验证码 Key。
6. 邮箱验证码发送成功后冷却 Key 生效。
7. 冷却期内重复发送被拒绝。
8. 邮箱验证码正确校验后 Key 被删除。
9. 邮箱验证码错误次数达到上限后验证码失效。
10. 登录不传图形验证码失败。
11. 登录图形验证码错误失败。
12. 登录密码错误会累计失败次数。
13. 登录失败达到上限后账号被锁定。
14. 登录成功后失败次数被清除。

验收标准：

- 核心单元测试通过。
- 接口测试覆盖主要成功和失败路径。
- 不依赖真实邮件发送完成核心逻辑测试，可以 mock `JavaMailSender`。

## 8. 文档和部署任务

### T16 更新项目文档和部署说明

目标：

- 补充 Redis 依赖和启动说明，避免部署时遗漏。

涉及文件：

```text
docs/dependencies.md
docs/api.md
README.md
```

执行内容：

1. 在依赖文档中增加 Redis 说明。
2. 在接口文档中更新登录接口请求参数。
3. 如果注册接口移除图形验证码参数，同步更新接口文档。
4. 在 README 中补充本地 Redis 启动说明。

验收标准：

- 文档与最终接口一致。
- 新开发者可以根据文档启动完整登录注册功能。

## 9. 推荐开发顺序

1. T01 增加 Redis 依赖和配置。
2. T02 新增认证配置类。
3. T03 新增 Redis Key 常量类。
4. T04 改造图形验证码。
5. T05 改造邮箱验证码。
6. T06 增加发送冷却。
7. T07 增加验证码错误次数限制。
8. T08 新增登录失败限制服务。
9. T09 改造登录 DTO。
10. T10 改造登录流程。
11. T11 梳理注册流程图形验证码校验策略。
12. T12 统一异常提示。
13. T13 补充测试。
14. T14、T15 前端联调。
15. T16 更新文档。

## 10. 最终验收清单

- 登录注册相关验证码全部使用 Redis 存储。
- 图形验证码和邮箱验证码都有 TTL。
- 验证码校验成功后会删除。
- 验证码错误次数限制生效。
- 邮箱验证码发送冷却生效。
- 登录必须输入图形验证码。
- 登录失败次数限制生效。
- 注册必须校验邮箱验证码。
- 注册密码和确认密码必须一致。
- 密码使用 BCrypt 保存。
- 接口异常提示友好且不泄露敏感信息。
- 前端登录页和注册页完成联调。
- 后端测试和接口测试通过。
- 文档与最终实现一致。
