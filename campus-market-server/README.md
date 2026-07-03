# campus-market-server

校园二手交易平台后端服务，基于 Spring Boot 3，提供认证、商品、分类、用户、管理员审核、图片上传和 AI 相关接口。

## 功能范围

- 用户注册、登录、获取当前用户信息
- 商品列表、商品详情、商品发布、上下架
- 分类列表
- 管理员商品审核、用户封禁/解封
- 图片上传与静态资源访问
- Swagger / OpenAPI 文档
- DeepSeek API 集成

## 技术栈

- Java 17
- Spring Boot 3.5.1
- MyBatis-Plus 3.5.7
- Sa-Token 1.39.0
- MySQL 8
- Maven 3.9+

## 环境要求

- JDK 17
- Maven 3.9+，或直接使用仓库自带的 `mvnw` / `mvnw.cmd`
- MySQL 8
- 可选：Docker / Docker Compose

## 目录结构

```text
src/main/java/com/campus/market/
├─ controller/   # API 控制器
├─ service/      # 业务层
├─ mapper/       # MyBatis-Plus mapper
├─ entity/       # 实体
├─ dto/          # 请求参数
├─ vo/           # 响应对象
├─ config/       # 配置
└─ common/       # 通用返回和异常处理

sql/
├─ init.sql          # 本地开发初始化脚本
└─ init-clean.sql    # 部署包使用的初始化脚本
```

## 关键控制器

- `AuthController`：登录注册
- `GoodsController`：商品列表、详情、发布、状态修改
- `CategoryController`：分类
- `UserController`：当前用户资料和个人商品
- `AdminController`：管理员审核和用户管理
- `UploadController`：文件上传
- `AiController`：AI 能力

## 本地启动

### 1. 准备数据库

先创建数据库并导入初始化脚本，默认库名是 `campus_market`。

如果你本地已经有 MySQL：

```sql
CREATE DATABASE campus_market CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

然后执行：

```powershell
mysql -uroot -p < sql\init.sql
```

也可以用 IDE 数据库工具执行 `sql/init.sql`。

### 2. 配置环境变量

应用通过环境变量读取配置，主要字段如下：

```text
SERVER_PORT=8080
DB_URL=jdbc:mysql://localhost:3306/campus_market?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
DB_USERNAME=root
DB_PASSWORD=你的数据库密码
JWT_SECRET_KEY=一段足够长的随机字符串
DEEPSEEK_API_KEY=sk-xxxx
UPLOAD_PATH=./upload
```

说明：

- `DEEPSEEK_API_KEY` 可为空；为空时 AI 相关功能不可用
- `JWT_SECRET_KEY` 不建议使用默认值
- `UPLOAD_PATH` 默认是项目目录下的 `upload`

### 3. 启动应用

使用 Maven Wrapper：

```powershell
.\mvnw.cmd spring-boot:run
```

或直接打包运行：

```powershell
.\mvnw.cmd clean package -DskipTests
java -jar target\market-0.0.1-SNAPSHOT.jar
```

启动成功后默认监听：

```text
http://localhost:8080
```

## 关于 Docker Compose

在这个部署仓库里，**统一部署入口是根目录的 `compose.yml`**，不是当前子目录下的 `compose.yml`。

原因是当前子目录里的 `compose.yml` 保留了原始多仓库开发时的目录约定，更适合原始项目结构，不适合作为这个整仓部署包的主入口。

如果你是在本仓库中部署，请回到根目录执行：

```powershell
cd ..
docker compose up -d --build
```

根目录还提供了：

- `.env.example`
- 两个前端的已构建 `dist`
- 正确的 Nginx 聚合部署路径

## 常用接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/hello` | 冒烟检查 |
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/login` | 用户登录 |
| GET | `/api/auth/me` | 当前登录用户 |
| GET | `/api/categories` | 分类列表 |
| GET | `/api/goods` | 商品列表 |
| GET | `/api/goods/{id}` | 商品详情 |
| POST | `/api/goods` | 发布商品 |
| GET | `/api/user/goods` | 我的商品 |
| PUT | `/api/admin/goods/{id}/approve` | 管理员审核通过 |
| PUT | `/api/admin/goods/{id}/reject` | 管理员驳回 |
| GET | `/api/admin/users` | 用户列表 |
| PUT | `/api/admin/users/{id}/ban` | 封禁用户 |
| PUT | `/api/admin/users/{id}/unban` | 解封用户 |

## 接口文档

- Swagger UI：`http://localhost:8080/swagger-ui.html`
- OpenAPI JSON：`http://localhost:8080/v3/api-docs`

## 默认账号

- 管理员：`admin / admin123`

初始化脚本中已预置该账号。

## 测试与构建

运行测试：

```powershell
.\mvnw.cmd test
```

打包：

```powershell
.\mvnw.cmd clean package
```

## 常见问题

### `api-key is empty`

没有配置 `DEEPSEEK_API_KEY`，或修改后没有重启应用。

### 数据库连接失败

优先检查：

- MySQL 是否真的启动
- `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 是否正确
- 数据库 `campus_market` 是否已创建

### 图片访问失败

检查：

- `UPLOAD_PATH` 目录是否存在并可写
- 前端访问的图片地址是否以 `/static/...` 开头
- 如果走 Docker，`upload_data` 卷是否正常挂载
