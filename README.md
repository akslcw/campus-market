# 校园二手交易平台部署包

这是一个可直接演示和部署的整仓库版本，包含后端、用户端前端、管理端前端三部分，适合直接上传到 GitHub 后给老师、评审或同学查看和运行。

## 包含内容

- `campus-market-server`：Spring Boot 3 后端
- `campus-market-web`：Vue 3 用户端
- `campus-market-admin`：Vue 3 管理端
- `compose.yml`：一键启动 MySQL、后端和 Nginx
- `.env.example`：部署时需要填写的环境变量模板

这个仓库已经提交了两个前端的 `dist` 产物，所以第一次演示不需要先单独构建前端。

## 技术栈

- Java 17
- Spring Boot 3.5
- MyBatis-Plus
- Sa-Token JWT
- MySQL 8
- Vue 3 + Vite
- Element Plus
- Docker Compose
- Nginx

## 目录结构

```text
campus_deploy/
├─ campus-market-server/   # 后端服务
├─ campus-market-web/      # 用户端前端
├─ campus-market-admin/    # 管理端前端
├─ compose.yml             # 根目录统一部署入口
├─ .env.example            # 环境变量示例
└─ README.md
```

## 快速启动

### 1. 克隆仓库

```powershell
git clone <https://github.com/akslcw/campus-market>
cd campus_deploy
```

### 2. 复制环境变量模板

```powershell
Copy-Item .env.example .env
```

### 3. 编辑 `.env`

```env
MYSQL_ROOT_PASSWORD=replace-with-a-strong-password
DEEPSEEK_API_KEY=sk-replace-with-your-key
JWT_SECRET_KEY=replace-with-a-long-random-secret
HTTP_PORT=80
```

字段说明：

- `MYSQL_ROOT_PASSWORD`：MySQL root 密码
- `DEEPSEEK_API_KEY`：AI 审核能力使用的 DeepSeek API Key；没有也能启动，但相关 AI 功能不可用
- `JWT_SECRET_KEY`：登录令牌签名密钥，建议改成足够长的随机字符串
- `HTTP_PORT`：对外暴露的 HTTP 端口，默认是 `80`

### 4. 启动服务

```powershell
docker compose up -d --build
```

### 5. 查看状态

```powershell
docker compose ps
docker compose logs -f
```

## 访问地址

假设服务器 IP 为 `127.0.0.1`，且 `HTTP_PORT=80`：

- 用户端：`http://127.0.0.1/`
- 管理端：`http://127.0.0.1/admin`
- Swagger：`http://127.0.0.1/swagger-ui.html`
- OpenAPI：`http://127.0.0.1/v3/api-docs`

如果你把 `HTTP_PORT` 改成了 `8088`，则地址需要写成：

- `http://127.0.0.1:8088/`
- `http://127.0.0.1:8088/admin`

## 默认演示账号

- 管理员：`admin / admin123`

普通用户可在用户端自行注册。

## 服务说明

`compose.yml` 会启动 3 个长期运行的容器：

- `mysql`：数据库，初始化脚本为 `campus-market-server/sql/init-clean.sql`
- `backend`：Spring Boot API 服务，内部端口 `8080`
- `nginx`：托管用户端和管理端静态资源，并反向代理 `/api`、`/static`、`/swagger-ui.html`

持久化数据：

- `mysql_data`：数据库文件
- `upload_data`：用户上传图片

## 本地开发

如果你想分别开发三个子项目，而不是直接跑部署包：

### 后端

```powershell
cd campus-market-server
.\mvnw.cmd spring-boot:run
```

### 用户端

```powershell
cd campus-market-web
npm install
npm run dev
```

默认开发地址：`http://localhost:8888`

### 管理端

```powershell
cd campus-market-admin
corepack enable
pnpm install
pnpm dev
```

默认开发地址：`http://localhost:8848`

两个前端开发模式都会把 `/api` 和 `/static` 代理到本地 `http://localhost:8080`。

## 重新构建前端

如果你改了前端源码，需要重新生成 `dist` 后再重新构建 Docker 镜像。

### 用户端

```powershell
cd campus-market-web
npm install
npm run build
```

### 管理端

```powershell
cd campus-market-admin
corepack enable
pnpm install
pnpm build
```

然后回到仓库根目录重新部署：

```powershell
cd ..
docker compose up -d --build
```

如果你当前就在根目录下的子目录中，请回到本仓库根目录再执行上面的命令。

## 常用运维命令

查看状态：

```powershell
docker compose ps
```

查看日志：

```powershell
docker compose logs -f
```

重启服务：

```powershell
docker compose restart
```

停止服务：

```powershell
docker compose down
```

停止并清空数据库、上传文件等卷数据：

```powershell
docker compose down -v
```

## 适合放到 GitHub 首页的说明点

如果你准备把这个仓库直接公开到 GitHub，建议保留这几个关键信息：

- 这是可直接运行的部署包，而不是只放源码
- 两个前端 `dist` 已经提交，适合演示
- 启动命令只有 3 步：复制 `.env`、填写配置、`docker compose up -d --build`
- 默认访问入口是 `/`、`/admin`、`/swagger-ui.html`

## 更多说明

各子项目的详细开发说明见：

- [campus-market-server/README.md](./campus-market-server/README.md)
- [campus-market-web/README.md](./campus-market-web/README.md)
- [campus-market-admin/README.md](./campus-market-admin/README.md)
