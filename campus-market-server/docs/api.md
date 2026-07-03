# 校园二手交易平台 — 后端接口文档

> 版本：V2.0 | 日期：2026-06-21 | 成员二整理
>
> Base URL: `http://localhost:8080`
>
> 统一返回：`{ "code": 200, "msg": "成功", "data": ... }`
>
> 鉴权：Header `Authorization: <token>`（登录获取）

---

## 通用约定

| 项目 | 说明 |
|------|------|
| 请求 Content-Type | `application/json`（上传除外） |
| 分页格式 | `{ "records": [...], "total": N, "size": N, "current": N, "pages": N }` |
| 鉴权方式 | Header: `Authorization: <token>`，Sa-Token JWT 无状态 |

### 统一错误码

| code | msg | 说明 |
|------|-----|------|
| 200 | 成功 | — |
| 400 | 参数错误 | 字段校验不通过 |
| 401 | 未登录 | Token 缺失或过期 |
| 403 | 无权限 | 角色不匹配或非本人操作 |
| 404 | 资源不存在 | — |
| 500 | 系统异常 | 兜底，稍后重试 |
| 1001 | 用户名已存在 | 注册时用户名重复 |
| 1002 | 用户不存在 | 登录时查无此人 |
| 1003 | 密码错误 | — |
| 1004 | 账号已被封禁 | 管理员封禁后无法登录 |
| 2001 | 商品不存在 | 查详情/操作时 ID 无效 |
| 2002 | 商品内容违规 | AI 检测不通过，附具体原因 |
| 3001 | AI 服务暂不可用 | DeepSeek 调用失败，自动降级 |

---

## 接口总览

| 编号 | 方法 | 路径 | 负责 | 鉴权 | 状态 |
|------|------|------|------|------|------|
| IF-01 | POST | `/api/auth/register` | 成员一/三 | 游客 | ✅ |
| IF-02 | POST | `/api/auth/login` | 成员一/三 | 游客 | ✅ |
| IF-03 | GET/PUT | `/api/user/profile` | 成员三 | 登录 | 🔲 |
| IF-04 | GET | `/api/goods` | **成员二** | 游客 | ✅ |
| IF-05 | GET | `/api/goods/{id}` | **成员二** | 游客 | ✅ |
| IF-06 | POST | `/api/goods` | **成员二** | 登录 | ✅ |
| IF-07 | PUT | `/api/goods/{id}/status` | **成员二** | 登录 | ✅ 选做 |
| IF-08 | POST | `/api/upload/image` | **成员二** | 登录 | ✅ |
| IF-09 | POST | `/api/ai/optimize` | 成员一 | 登录 | ✅ |
| IF-10 | POST | `/api/ai/check` | 成员一 | 登录 | ✅ 内部 |
| IF-11 | GET/PUT | `/api/admin/goods/audit` | 成员三 | 管理员 | 🔲 |
| IF-12 | GET/PUT | `/api/admin/users` | 成员三 | 管理员 | 🔲 |
| IF-13 | POST/DELETE/GET | `/api/favorite` | **成员二** | 登录 | 🔲 选做 |

> ✅ = 已实现　🔲 = 待实现

---

## IF-01 用户注册

```
POST /api/auth/register
```

**请求体**

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| username | string | 是 | 3-20 位 | 用户名 |
| password | string | 是 | 6-32 位 | 密码（BCrypt 加密存储） |
| nickname | string | 否 | — | 昵称，默认同 username |

**响应** `data = null`

```json
{ "code": 200, "msg": "成功", "data": null }
```

**错误**：1001 用户名已存在

---

## IF-02 用户登录

```
POST /api/auth/login
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |

**响应 data (LoginVO)**

| 字段 | 类型 | 说明 |
|------|------|------|
| token | string | JWT Token，后续请求放 Header |
| userId | long | 用户 ID |
| username | string | 用户名 |
| nickname | string | 昵称 |
| role | string | USER / ADMIN |

```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "token": "eyJ...",
    "userId": 1,
    "username": "testuser",
    "nickname": "测试用户",
    "role": "USER"
  }
}
```

**错误**：1002 用户不存在 | 1003 密码错误 | 1004 账号已封禁

---

## IF-03 个人中心

```
GET    /api/user/profile    查看个人信息
PUT    /api/user/profile    修改个人信息
```

### GET — 返回 data

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 用户 ID |
| username | string | 用户名 |
| nickname | string | 昵称 |
| avatar | string | 头像 URL |
| contact | string | 联系方式 |
| role | string | USER / ADMIN |

### PUT — 请求体

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| nickname | string | 否 | 昵称 |
| avatar | string | 否 | 头像 URL |
| contact | string | 否 | 联系方式 |

```json
{ "code": 200, "msg": "成功", "data": null }
```

> 负责人：成员三，待实现

---

## IF-04 商品列表

```
GET /api/goods
```

**请求参数 Query**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| categoryId | integer | 否 | — | 分类 ID 筛选 |
| keyword | string | 否 | — | 关键词（匹配标题+描述） |
| status | string | 否 | ON_SALE | ON_SALE / SOLD / OFF_SHELF |
| auditStatus | string | 否 | APPROVED | PENDING / APPROVED / REJECTED |
| sellerId | long | 否 | — | 查某用户发布 |
| page | integer | 否 | 1 | 页码 |
| size | integer | 否 | 10 | 每页条数 |
| sort | string | 否 | newest | newest / price_asc / price_desc |

**返回 data（分页）**

| 字段 | 类型 | 说明 |
|------|------|------|
| records | array | 商品列表 |
| records[].id | long | 商品 ID |
| records[].title | string | 标题 |
| records[].price | number | 售价 |
| records[].originalPrice | number | 原价 |
| records[].categoryId | integer | 分类 ID |
| records[].categoryName | string | 分类名称 |
| records[].status | string | ON_SALE / SOLD / OFF_SHELF |
| records[].viewCount | integer | 浏览次数 |
| records[].coverImage | string | 首张封面图 URL |
| records[].sellerName | string | 卖家用户名 |
| records[].createdAt | string | 创建时间 |
| total | long | 总条数 |
| pages | long | 总页数 |
| current | long | 当前页 |
| size | long | 每页条数 |

```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "records": [
      {
        "id": 1,
        "title": "二手 iPhone 14 128G",
        "price": 3500.00,
        "originalPrice": 5999.00,
        "categoryId": 1,
        "categoryName": "数码电子",
        "status": "ON_SALE",
        "viewCount": 128,
        "coverImage": "/static/202606/21/uuid.jpg",
        "sellerName": "testuser",
        "createdAt": "2026-06-21T10:30:00"
      }
    ],
    "total": 1, "pages": 1, "current": 1, "size": 10
  }
}
```

> 负责人：成员二 ✅

---

## IF-05 商品详情

```
GET /api/goods/{id}
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | long | 是 | 商品 ID |

**返回 data**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 商品 ID |
| title | string | 标题 |
| description | string | 描述 |
| rawDescription | string | 用户原始描述 |
| price | number | 售价 |
| originalPrice | number | 原价 |
| categoryId | integer | 分类 ID |
| categoryName | string | 分类名称 |
| images | array[string] | 全部图片 URL |
| status | string | ON_SALE / SOLD / OFF_SHELF |
| auditStatus | string | PENDING / APPROVED / REJECTED |
| auditRemark | string | 审核拒绝原因 |
| viewCount | integer | 浏览次数（每次+1） |
| sellerId | long | 卖家 ID |
| sellerName | string | 卖家用户名 |
| sellerContact | string | 联系方式 |
| createdAt | string | 创建时间 |
| updatedAt | string | 更新时间 |

```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "id": 1,
    "title": "二手 iPhone 14 128G",
    "description": "出一台自用 iPhone 14 128G 黑色，使用一年，外观轻微划痕，功能一切正常。配件含原装充电器、数据线、包装盒。仅限校内面交。",
    "rawDescription": "出手机",
    "price": 3500.00,
    "originalPrice": 5999.00,
    "categoryId": 1,
    "categoryName": "数码电子",
    "images": ["/static/202606/21/uuid1.jpg", "/static/202606/21/uuid2.jpg"],
    "status": "ON_SALE",
    "auditStatus": "APPROVED",
    "auditRemark": null,
    "viewCount": 129,
    "sellerId": 4,
    "sellerName": "testuser",
    "sellerContact": "QQ: 123456",
    "createdAt": "2026-06-21T10:30:00",
    "updatedAt": "2026-06-21T20:00:00"
  }
}
```

**错误**：2001 商品不存在

> 负责人：成员二 ✅

---

## IF-06 发布商品

```
POST /api/goods
```

**说明**：发布新品，内置自动调用 IF-10 AI 违规检测。检测违规返回 2002；AI 明确判定正常时 auditStatus 为 APPROVED；AI 调用失败或结果无法解析时降级为 PENDING，交由管理员人工审核。status 默认 ON_SALE。

**请求体**

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| title | string | 是 | 2-100 位 | 商品标题 |
| description | string | 是 | — | 描述（建议先用 IF-09 AI 优化） |
| rawDescription | string | 否 | — | 用户原始描述 |
| price | number | 是 | 0.01-99999.99 | 售价 |
| originalPrice | number | 否 | — | 原价 |
| categoryId | integer | 是 | — | 分类 ID |
| categoryName | string | 是 | — | 分类名称 |
| images | array[string] | 是 | 1-5 张 | 图片 URL（先调 IF-08 上传） |
| contact | string | 否 | — | 不填则用用户资料中的 |

```json
{
  "title": "二手 iPhone 14 128G",
  "description": "出一台自用 iPhone 14 128G 黑色，使用一年，外观轻微划痕，功能一切正常。",
  "rawDescription": "出手机，便宜卖了",
  "price": 3500.00,
  "originalPrice": 5999.00,
  "categoryId": 1,
  "categoryName": "数码电子",
  "images": ["/static/202606/21/abc.jpg", "/static/202606/21/def.jpg"],
  "contact": "QQ: 123456"
}
```

**返回 data**：同 IF-05 商品详情。

**错误**：2002 商品内容违规（附 AI 检测原因，如"涉及售卖电子烟"）

> 负责人：成员二 ✅ · 关键亮点：内置 AI 违规检测

---

## IF-07 商品状态流转

```
PUT /api/goods/{id}/status
```

**说明**：卖家标记已售或下架，仅本人可操作，仅"在售"商品可流转。选做功能。

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | long | 是 | 商品 ID |

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | string | 是 | SOLD（已售） / OFF_SHELF（下架） |

```json
{ "status": "SOLD" }
```

```json
{ "code": 200, "msg": "成功", "data": null }
```

**错误**：403 非卖家本人 | 400 商品不在售

> 负责人：成员二 ✅ 选做

---

## IF-08 图片上传

```
POST /api/upload/image
```

**说明**：Content-Type: multipart/form-data。前端逐张上传，收集 URL 后传入 IF-06。

**请求参数 form-data**

| 参数 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| file | file | 是 | ≤10MB, image/* | 图片文件 |

**返回 data**

| 类型 | 说明 |
|------|------|
| string | 图片访问 URL |

```json
{ "code": 200, "msg": "成功", "data": "/static/202606/21/a1b2c3d4e5f6.jpg" }
```

> 负责人：成员二 ✅

---

## IF-09 描述一键优化

```
POST /api/ai/optimize
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | string | 否 | 商品标题 |
| description | string | 是 | 用户原始描述 |

```json
{
  "title": "出二手 iPhone",
  "description": "用了一年，有点划痕，便宜出了"
}
```

**返回 data**：string，50-100 字优化后描述。

```json
{
  "code": 200,
  "msg": "成功",
  "data": "出一台自用 iPhone 14 128G 黑色，使用一年，外观轻微划痕，功能一切正常。配件含原装充电器、数据线。仅限校内面交，诚心要可小刀。"
}
```

**失败处理**：AI 调用超时或返回异常时返回错误码 3001，不再将原始描述包装成优化成功。

> 负责人：成员一 ✅ · 答辩最强亮点

---

## IF-10 违规内容检测

```
POST /api/ai/check
```

**说明**：检测商品是否包含违禁内容。IF-06 发布时内置调用，前端也可独立调用做前置校验。

**检测规则**：违禁品 / 色情低俗 / 暴恐涉政 / 学术作弊 / 隐私泄露 / 欺诈 / 广告引流

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | string | 否 | 商品标题 |
| description | string | 否 | 商品描述 |

**返回 data (ContentCheckVO)**

| 字段 | 类型 | 说明 |
|------|------|------|
| violation | boolean | 是否违规 |
| reason | string | 违规原因（违规时有值） |
| category | string | 违规类别（违规时有值） |

```json
// 通过
{ "code": 200, "msg": "成功", "data": { "violation": false, "reason": "", "category": "" } }

// 违规
{ "code": 200, "msg": "成功", "data": { "violation": true, "reason": "涉及售卖电子烟", "category": "违禁品" } }
```

**降级**：AI 调用失败时默认放行（violation=false），由人工审核兜底。

> 负责人：成员一 ✅ · 标注为内部调用

---

## IF-11 商品审核

```
GET    /api/admin/goods/audit       待审核商品列表
PUT    /api/admin/goods/{id}/approve   审核通过
PUT    /api/admin/goods/{id}/reject    审核拒绝
```

### GET — 返回 data（分页）

返回 auditStatus = PENDING 的商品列表，同 IF-04 结构。

### PUT approve — 无请求体

```json
{ "code": 200, "msg": "成功", "data": null }
```

### PUT reject — 请求体

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| reason | string | 是 | 拒绝原因 |

> 负责人：成员三，待实现 — 可调用成员二的 `GoodsService.audit()`

---

## IF-12 用户管理

```
GET    /api/admin/users              用户列表
PUT    /api/admin/users/{id}/ban      封禁用户
PUT    /api/admin/users/{id}/unban    解封用户
```

### GET — 返回 data（分页）

### PUT ban/unban — 无请求体

```json
{ "code": 200, "msg": "成功", "data": null }
```

> 负责人：成员三，待实现

---

## IF-13 收藏商品（选做）

```
POST   /api/favorite/{goodsId}        收藏商品
DELETE /api/favorite/{goodsId}        取消收藏
GET    /api/favorite                  我的收藏列表
```

> 负责人：成员二，待实现 — 选做功能，时间充裕时补充

---

## 补充接口

### 获取分类列表

```
GET /api/categories
```

```json
{
  "code": 200,
  "msg": "成功",
  "data": [
    { "id": 1, "name": "数码电子", "sortOrder": 1 },
    { "id": 2, "name": "书籍教材", "sortOrder": 2 },
    { "id": 3, "name": "生活用品", "sortOrder": 3 },
    { "id": 4, "name": "服饰鞋包", "sortOrder": 4 },
    { "id": 5, "name": "运动户外", "sortOrder": 5 },
    { "id": 6, "name": "美妆护肤", "sortOrder": 6 },
    { "id": 7, "name": "食品饮料", "sortOrder": 7 },
    { "id": 8, "name": "其他", "sortOrder": 99 }
  ]
}
```

---

## 状态枚举

| 枚举 | 值 | 说明 |
|------|-----|------|
| 商品状态 status | ON_SALE | 在售 |
| | SOLD | 已售 |
| | OFF_SHELF | 已下架 |
| 审核状态 auditStatus | PENDING | 待审核 |
| | APPROVED | 审核通过（前台可见） |
| | REJECTED | 审核拒绝（仅卖家+管理员可见） |
| 用户角色 role | USER | 普通用户 |
| | ADMIN | 管理员 |

---

## 答辩演示链路

```
① IF-01 POST /api/auth/register       → 注册新账号
② IF-02 POST /api/auth/login          → 登录，拿到 Token
③ IF-09 POST /api/ai/optimize         → AI 优化描述（最强亮点）
④ IF-08 POST /api/upload/image ×N     → 逐张上传图片
⑤ IF-06 POST /api/goods               → 发布商品（内置 AI 违规检测）
⑥ IF-11 PUT  /api/admin/goods/{id}/approve → 管理员审核通过
⑦ IF-04 GET  /api/goods               → 浏览商品列表（搜索/筛选）
⑧ IF-05 GET  /api/goods/{id}          → 手机端查看详情（H5 适配）
```

---

## 变更记录

| 日期 | 版本 | 变更 |
|------|------|------|
| 2026-06-21 | V2.0 | IF 编号统一对齐，新增 IF-03/IF-11/IF-12/IF-13，IF-06 标注内置 AI 检测 |
| 2026-06-21 | V1.0 | 初版，成员二接口整理 |
