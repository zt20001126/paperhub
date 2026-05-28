# PaperHub 接口文档

后端默认地址：

```text
http://localhost:8080
```

## 通用返回结构

所有接口返回 `Result<T>`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `success` | `boolean` | 是否成功 |
| `message` | `string` | 返回消息 |
| `data` | `any` | 返回数据，可能为 `null`、对象或数组 |

成功示例：

```json
{
  "success": true,
  "message": "OK",
  "data": {}
}
```

## 认证模块

### 获取图片验证码

- 接口名称：获取图片验证码
- 请求方式：`GET`
- 路径：`/api/auth/captcha`

请求参数：无

返回参数：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `captchaId` | `string` | 图片验证码 ID |
| `imageBase64` | `string` | PNG 图片 Base64，带 `data:image/png;base64,` 前缀 |
| `expiresInSeconds` | `number` | 过期秒数 |

成功示例：

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "captchaId": "6db174b0d2f54bd9a5e2c2a9e3f07d43",
    "imageBase64": "data:image/png;base64,...",
    "expiresInSeconds": 300
  }
}
```

### 发送邮箱验证码

- 接口名称：发送邮箱验证码
- 请求方式：`POST`
- 路径：`/api/auth/send-code`

请求参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `email` | `string` | 是 | 注册邮箱 |
| `captchaId` | `string` | 是 | 图片验证码 ID |
| `captchaCode` | `string` | 是 | 用户输入的图片验证码 |

返回参数：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `data` | `null` | 成功时为空 |

成功示例：

```json
{
  "success": true,
  "message": "OK",
  "data": null
}
```

失败场景：

- 邮箱格式不正确
- 图片验证码错误或过期
- 邮箱已注册
- SMTP 邮件发送失败

### 注册

- 接口名称：用户注册
- 请求方式：`POST`
- 路径：`/api/auth/register`

请求参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `email` | `string` | 是 | 注册邮箱 |
| `emailCode` | `string` | 是 | 邮箱验证码 |
| `password` | `string` | 是 | 密码，8 到 50 位，必须同时包含字母和数字 |
| `confirmPassword` | `string` | 是 | 确认密码，必须与 `password` 一致 |
| `captchaId` | `string` | 是 | 图片验证码 ID |
| `captchaCode` | `string` | 是 | 用户输入的图片验证码 |

返回参数：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `data` | `null` | 成功时为空 |

成功示例：

```json
{
  "success": true,
  "message": "OK",
  "data": null
}
```

失败场景：

- 邮箱已注册
- 邮箱验证码错误或过期
- 图片验证码错误或过期
- 密码不符合规则
- 两次密码不一致

### 登录

- 接口名称：用户登录
- 请求方式：`POST`
- 路径：`/api/auth/login`

请求参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `email` | `string` | 是 | 登录邮箱 |
| `password` | `string` | 是 | 登录密码 |

返回参数：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `number` | 用户 ID |
| `email` | `string` | 邮箱 |
| `nickname` | `string` | 昵称 |
| `avatarUrl` | `string` | 头像地址 |
| `points` | `number` | 积分 |
| `token` | `string` | 登录 token |

成功示例：

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "nickname": "user",
    "avatarUrl": "",
    "points": 100,
    "token": "token"
  }
}
```

### 修改用户信息

- 接口名称：修改用户信息
- 请求方式：`PUT`
- 路径：`/api/auth/update_user_info`

请求头：

| 名称 | 必填 | 说明 |
| --- | --- | --- |
| `Authorization` | 是 | `Bearer <token>` |

请求参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `avatarUrl` | `string` | 否 | 头像地址，最长 255 |
| `nickname` | `string` | 否 | 昵称，2 到 20 个字符 |

返回参数：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `data` | `null` | 成功时为空 |

## 文献求助模块

### 分页查询文献求助

- 接口名称：分页查询文献求助
- 请求方式：`GET`
- 路径：`/api/lit-request/page`

请求参数：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `current` | `number` | 否 | `1` | 当前页 |
| `size` | `number` | 否 | `10` | 每页条数 |

返回参数：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `records` | `array` | 文献求助列表 |
| `total` | `number` | 总条数 |
| `current` | `number` | 当前页 |
| `size` | `number` | 每页条数 |
| `pages` | `number` | 总页数 |

`records` 单项字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `number` | 文献求助 ID |
| `userId` | `number` | 发布用户 ID |
| `title` | `string` | 论文标题 |
| `journal` | `string` | 期刊 |
| `doi` | `string` | DOI |
| `rewardPoints` | `number` | 奖励积分 |
| `status` | `number` | 状态 |
| `createTime` | `string` | 创建时间 |
| `updateTime` | `string` | 更新时间 |

### 发布文献求助

- 接口名称：发布文献求助
- 请求方式：`POST`
- 路径：`/api/lit-request/sub_lit`

请求参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `userId` | `number` | 是 | 发布用户 ID |
| `title` | `string` | 是 | 论文标题 |
| `journal` | `string` | 是 | 发表期刊 |
| `doi` | `string` | 是 | DOI |
| `rewardPoints` | `number` | 是 | 奖励积分，不能小于 0 |

返回参数：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `data` | `null` | 成功时为空 |

失败场景：

- 用户不存在
- 用户积分不足
- 参数为空或积分小于 0

### 我要应助

- 接口名称：提交应助 PDF
- 请求方式：`POST`
- 路径：`/api/lit-request/assist`
- 请求类型：`multipart/form-data`
- 请求头：`Authorization: Bearer <token>`

请求参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `litRequestId` | `number` | 是 | 文献求助 ID |
| `file` | `file` | 是 | 只能上传 `.pdf` 文件 |

返回参数：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `data` | `null` | 成功时为空 |

失败场景：

- 未登录或登录状态失效
- 文献求助不存在
- 未上传文件
- 文件名不是 `.pdf` 结尾
- 文件内容不是有效 PDF
- MinIO 存储桶不可用或上传失败

## 热点话题模块

### 查询热点列表

- 接口名称：查询热点列表
- 请求方式：`GET`
- 路径：`/api/hot_topic_page`

请求参数：无

返回参数：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `number` | 热点 ID |
| `title` | `string` | 标题 |
| `content` | `string` | 内容 |
| `coverUrl` | `string` | 封面地址 |
| `viewCount` | `number` | 浏览数 |
| `publishTime` | `string` | 发布时间 |
| `updateTime` | `string` | 更新时间 |

### 热点浏览量加一

- 接口名称：热点浏览量加一
- 请求方式：`POST`
- 路径：`/api/hot_topic_view`

请求参数：

| 字段 | 类型 | 必填 | 位置 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | `number` | 是 | query | 热点 ID |

返回参数：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `data` | `null` | 成功时为空 |

## 社区帖子模块

### 发布帖子

- 接口名称：发布帖子
- 请求方式：`POST`
- 路径：`/api/group/post/publish`

请求参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `userId` | `number` | 是 | 发布用户 ID |
| `title` | `string` | 是 | 帖子标题 |
| `content` | `string` | 是 | 帖子内容 |

返回参数：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `data` | `null` | 成功时为空 |

### 查询帖子列表

- 接口名称：查询帖子列表
- 请求方式：`GET`
- 路径：`/api/group/post/list`

请求参数：无

返回参数：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `number` | 帖子 ID |
| `userId` | `number` | 发布用户 ID |
| `userNickname` | `string` | 发布用户昵称 |
| `userAvatarUrl` | `string` | 发布用户头像 |
| `title` | `string` | 帖子标题 |
| `content` | `string` | 帖子内容 |
| `likeCount` | `number` | 点赞数 |
| `viewCount` | `number` | 浏览数 |
| `publishTime` | `string` | 发布时间 |

## 当前限制

我要应助当前支持 PDF 提交和 MinIO 保存，暂未实现发布者审核、下载、采纳和积分结算。
