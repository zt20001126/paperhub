# PaperHub Web

Vue 3 + Vite 前端项目，主页面位于 `src/App.vue`。

## 我要应助 PDF 上传

- 文献详情页点击“我要应助”后，只允许选择 `.pdf` 文件。
- 提交接口：`POST /api/lit-request/assist`。
- 开发环境通过 Vite `/api` 代理访问后端 `8080` 端口。

## 启动与构建

```bash
npm install
npm run dev
npm run build
```
