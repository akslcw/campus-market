# campus-market-admin

校园二手交易平台管理端，负责管理员登录、商品审核和用户管理。

## 功能范围

- 管理员登录
- 待审核商品列表
- 商品详情查看
- 审核通过 / 驳回
- 用户列表
- 用户封禁 / 解封

生产部署时，管理端挂载在 `/admin/` 路径下。

## 技术栈

- Vue 3
- TypeScript
- Vite 7
- Pinia
- Element Plus
- Pure Admin Lite 作为基础后台框架
- Vitest

## 环境要求

- Node.js 20+
- pnpm 9+，建议直接使用 `corepack`

## 本地运行

### 1. 启用 pnpm

```powershell
corepack enable
```

### 2. 安装依赖

```powershell
pnpm install
```

### 3. 启动开发服务器

```powershell
pnpm dev
```

默认地址：

```text
http://127.0.0.1:8848
```

## 默认账号

- 管理员：`admin / admin123`

## 接口联调

开发模式下，以下路径会被 Vite 代理到本地后端：

- `/api`
- `/static`

目标地址：

```text
http://localhost:8080
```

因此联调前需要先启动后端。

## 生产构建

```powershell
pnpm build
```

构建结果输出到：

```text
dist/
```

根仓库部署时，Nginx 会把这个目录挂到 `/admin/`。

## 验证命令

运行测试：

```powershell
pnpm test
```

类型检查：

```powershell
pnpm typecheck
```

构建检查：

```powershell
pnpm build
```

## 主要目录

```text
src/
├─ api/         # 管理端接口
├─ layout/      # 后台布局
├─ router/      # 路由
├─ store/       # 状态管理
├─ views/
│  ├─ login/    # 登录页
│  ├─ welcome/  # 首页概览
│  └─ admin/    # 商品审核、用户管理
└─ style/
```

## 关键说明

- 登录实际调用的是统一后端接口 `POST /api/auth/login`
- 审核商品接口在 `/api/admin/goods/*`
- 用户管理接口在 `/api/admin/users/*`
- 本地开发使用 Hash 路由
- 生产环境 `VITE_PUBLIC_PATH` 为 `/admin/`

## 常见命令

启动开发环境：

```powershell
pnpm dev
```

单次测试：

```powershell
pnpm test
```

格式化与静态检查：

```powershell
pnpm lint
```

## 常见问题

### 管理端可以打开但接口都 404

优先检查后端是否运行在 `http://localhost:8080`，因为本地开发代理写死到了这个地址。

### 部署后打开 `/admin` 白屏

优先检查：

- 是否执行过 `pnpm build`
- Nginx 是否把 `dist` 内容挂到了 `/admin/`
- `VITE_PUBLIC_PATH` 是否保持为 `/admin/`
