# campus-market-web

校园二手交易平台用户端前端，基于 Vue 3 + Vite，面向普通用户提供浏览、发布和个人中心能力。

## 功能概览

- 首页商品列表、分类筛选、关键词搜索、排序、分页
- 商品详情页
- 用户注册、登录
- 商品发布
- 个人中心和个人商品管理
- 联系卖家信息展示

首页筛选和分页状态保存在 URL query 中，返回列表页时可以恢复原来的页码和筛选条件。

## 技术栈

- Vue 3.5
- TypeScript
- Vite 8
- Vue Router
- Pinia
- Element Plus
- Axios

## 环境要求

- Node.js 20+
- npm 10+（本项目直接用 npm 即可）

## 本地开发

### 1. 安装依赖

```powershell
npm install
```

### 2. 启动开发服务器

```powershell
npm run dev
```

默认地址：

```text
http://localhost:8888
```

### 3. 启动后端

开发模式下，Vite 会把：

- `/api`
- `/static`

代理到：

```text
http://localhost:8080
```

所以你需要先确保后端已经在 `8080` 端口运行。

## 构建

```powershell
npm run build
```

构建产物输出到：

```text
dist/
```

如果这个前端要被根目录 `compose.yml` 使用，记得保留 `dist` 目录。

## 页面路由

- `/login`：登录
- `/register`：注册
- `/home`：首页
- `/goods/:id`：商品详情
- `/publish`：发布商品
- `/profile`：个人中心

根路径 `/` 会重定向到 `/home`。

## 主要目录

```text
src/
├─ api/          # 接口请求封装
├─ composables/  # 组合式逻辑
├─ router/       # 路由
├─ stores/       # Pinia 状态
├─ views/        # 页面
├─ utils/        # 请求实例等通用工具
├─ App.vue
└─ main.ts
```

## 关键实现说明

- 统一请求实例位于 `src/utils/request.ts`
- 业务 API 按模块拆分在 `src/api/*`
- 受保护页面会在未登录时跳转到 `/login`
- 登录成功后支持按 `redirect` 参数跳回原目标页面

## 常用命令

启动开发环境：

```powershell
npm run dev
```

生产构建：

```powershell
npm run build
```

本地预览构建结果：

```powershell
npm run preview
```

## 联调说明

前端依赖后端接口：

- 登录：`POST /api/auth/login`
- 注册：`POST /api/auth/register`
- 首页商品：`GET /api/goods`
- 商品详情：`GET /api/goods/{id}`
- 发布商品：`POST /api/goods`
- 个人商品：`GET /api/user/goods`

如果登录、发布或图片显示异常，优先检查后端和 `/static` 资源映射是否正常。

## 常见问题

### 登录提示网络异常

通常是后端没有启动，或者后端不在 `http://localhost:8080`。

### 图片不显示

优先检查：

- 返回的图片地址是否以 `/static/...` 开头
- 后端上传目录是否存在
- Nginx 或本地代理是否把 `/static` 正确转发到了后端

### 跳回列表后页码丢失

这个项目当前使用 URL query 保存首页状态。如果你改了首页跳转逻辑，注意不要丢掉：

- `page`
- `categoryId`
- `sort`
- `keyword`
