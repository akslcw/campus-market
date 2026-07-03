# 商品模块接口实现说明

> 校园二手交易平台 — 商品发布、图片上传、列表查询、详情、搜索、下架、AI 违规检测

---

## 一、涉及文件清单

```
src/main/java/com/campus/market/
├── controller/
│   ├── GoodsController.java          # 商品接口入口
│   └── UploadController.java         # 图片上传入口
├── service/
│   ├── GoodsService.java             # 商品服务接口
│   ├── FileStorageService.java       # 本地文件存储
│   └── impl/
│       ├── GoodsServiceImpl.java     # 商品服务实现（核心逻辑）
│       └── GoodsImageServiceImpl.java# 图片关联服务
├── entity/
│   ├── Goods.java                    # 商品实体（映射 goods 表）
│   └── GoodsImage.java               # 商品图片实体（映射 goods_image 表）
├── dto/
│   ├── GoodsCreateDTO.java           # 发布请求体
│   └── GoodsQueryDTO.java            # 列表查询参数
├── vo/
│   ├── GoodsVO.java                  # 列表项响应
│   └── GoodsDetailVO.java            # 详情响应
└── config/
    ├── SaTokenConfig.java            # 认证拦截 & 静态资源映射
    └── UploadProperties.java         # 上传路径配置
```

---

## 二、功能实现详解

### 2.1 POST /api/goods — 发布商品

**入口：** `controller/GoodsController.java:32-38`

```java
@Operation(summary = "IF-06 发布商品", description = "发布新品(自动触发AI违规检测)，状态默认「审核中」")
@PostMapping
public Result<GoodsDetailVO> publish(@Valid @RequestBody GoodsCreateDTO dto) {
    Long userId = StpUtil.getLoginIdAsLong();
    GoodsDetailVO vo = goodsService.publish(dto, userId);
    return Result.success(vo);
}
```

- `@Valid` 触发 DTO 校验
- `StpUtil.getLoginIdAsLong()` 从 JWT 中取当前用户 ID
- 调用 `goodsService.publish(dto, userId)` 执行业务

#### 2.1.1 参数校验 — `dto/GoodsCreateDTO.java`

```java
@Data
public class GoodsCreateDTO {
    @NotBlank(message = "标题不能为空")
    @Size(min = 2, max = 100, message = "标题长度需为 2-100 位")
    private String title;

    @NotBlank(message = "描述不能为空")
    private String description;

    private String rawDescription;          // 用户原始描述（可选）

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于 0")
    @DecimalMax(value = "99999.99", message = "价格不能超过 99999.99")
    private BigDecimal price;

    private BigDecimal originalPrice;       // 原价（可选）

    @NotNull(message = "分类不能为空")
    private Integer categoryId;

    @NotBlank(message = "分类名称不能为空")
    private String categoryName;

    @Size(min = 1, max = 5, message = "图片数量需为 1-5 张")
    private List<String> images;            // 先调上传接口拿 URL

    private String contact;                 // 联系方式（不填则取用户资料中的）
}
```

说明：
- 标题 2–100 字符，价格 0.01–99999.99，图片 1–5 张
- 违反约束时 `GlobalExceptionHandler` 捕获 `MethodArgumentNotValidException`，统一返回 400 + 字段错误信息

#### 2.1.2 核心逻辑 — `service/impl/GoodsServiceImpl.java:54-92`

```java
@Override
@Transactional
public GoodsDetailVO publish(GoodsCreateDTO dto, Long userId) {
    // 1. 校验用户存在
    User seller = userService.getById(userId);
    if (seller == null) {
        throw new BusinessException(ResultCode.USER_NOT_FOUND);
    }

    // 2. AI 违规检测（与组长对接）—— 不通过直接拒绝
    ContentCheckVO checkResult = aiService.checkContent(dto.getTitle(), dto.getDescription());
    if (Boolean.TRUE.equals(checkResult.getViolation())) {
        throw new BusinessException(ResultCode.GOODS_VIOLATION.getCode(),
                "内容违规: " + checkResult.getReason());
    }

    // 3. 组装实体并入库
    Goods goods = new Goods();
    goods.setTitle(dto.getTitle());
    goods.setDescription(dto.getDescription());
    goods.setRawDescription(dto.getRawDescription());
    goods.setPrice(dto.getPrice());
    goods.setOriginalPrice(dto.getOriginalPrice());
    goods.setCategoryId(dto.getCategoryId());
    goods.setCategoryName(dto.getCategoryName());
    goods.setSellerId(userId);
    goods.setSellerName(seller.getUsername());
    goods.setSellerContact(
            dto.getContact() != null ? dto.getContact() : seller.getContact());
    goods.setStatus(ON_SALE);             // 默认「在售」
    goods.setAuditStatus(AUDIT_PENDING);  // 默认「审核中」
    goods.setViewCount(0);
    save(goods);  // MyBatis-Plus 内置方法

    // 4. 保存关联图片（goods_image 表）
    goodsImageService.saveImages(goods.getId(), dto.getImages());

    log.info("商品发布成功: id={}, title={}, seller={}", goods.getId(), goods.getTitle(), userId);
    return getDetail(goods.getId());
}
```

执行流程：

```
@Valid 校验参数
    │
    ▼
StpUtil 取登录用户
    │
    ▼
查用户是否存在 ──→ 不存在 → 抛 USER_NOT_FOUND
    │ 存在
    ▼
AI 违规检测 ──→ 违规 → 抛 GOODS_VIOLATION（拒绝提交）
    │ 通过
    ▼
组装 goods, status=ON_SALE, auditStatus=PENDING
    │
    ▼
save(goods) → goods 表
    │
    ▼
saveImages → goods_image 表（1~5条）
    │
    ▼
返回 GoodsDetailVO（含完整信息）
```

- `@Transactional` 保证 goods 和 goods_image 两表原子写入
- AI 检测失败只回滚到 AI 调用之前（事务尚未开始写库），不会产生脏数据

---

### 2.2 POST /api/upload/image — 图片上传

**入口：** `controller/UploadController.java:27-38`

```java
@PostMapping("/image")
public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
    if (file.isEmpty()) {
        return Result.fail(400, "文件不能为空");
    }
    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
        return Result.fail(400, "仅支持图片上传");
    }
    String url = fileStorageService.save(file);
    return Result.success(url);
}
```

- 校验文件非空 + MIME 类型以 `image/` 开头
- 委托 `FileStorageService.save()` 完成存储

#### 2.2.1 存储逻辑 — `service/FileStorageService.java:30-55`

```java
public String save(MultipartFile file) {
    // 生成相对路径：goods/yyyyMM/dd/uuid.扩展名
    String dateDir = LocalDate.now().format(
            DateTimeFormatter.ofPattern("yyyyMM/dd"));
    String ext = getExtension(file.getOriginalFilename());
    String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
    String relativePath = "goods/" + dateDir + "/" + filename;

    // 写入磁盘
    Path fullPath = Paths.get(uploadProperties.getPath(), relativePath);
    Files.createDirectories(fullPath.getParent());
    file.transferTo(fullPath.toFile());

    // 返回可访问 URL
    return uploadProperties.getUrlPrefix() + "/" + relativePath.replace("\\", "/");
}
```

示例：上传 `photo.jpg` → 存储到 `./upload/goods/202606/22/a1b2c3d4e5f6.jpg` → 返回 `/static/goods/202606/22/a1b2c3d4e5f6.jpg`

#### 2.2.2 静态资源映射 — `config/SaTokenConfig.java:32-35`

```java
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler(uploadProperties.getUrlPrefix() + "/**")
            .addResourceLocations("file:" + uploadProperties.getPath() + "/");
}
```

将 URL `/static/**` 映射到磁盘 `file:./upload/`，上传的图片可通过 HTTP 直接访问。

#### 2.2.3 上传配置 — `config/UploadProperties.java`

```java
@ConfigurationProperties(prefix = "upload")
public class UploadProperties {
    private String path = "./upload";       // 存储根目录
    private String urlPrefix = "/static";   // URL 前缀
}
```

对应 `application.yml`：

```yaml
upload:
  path: ./upload
  url-prefix: /static

spring.servlet.multipart:
  max-file-size: 10MB
  max-request-size: 50MB
```

---

### 2.3 GET /api/goods/list — 分页列表

**入口：** `controller/GoodsController.java:47-52`

```java
@GetMapping("/list")
public Result<IPage<GoodsVO>> listGoods(GoodsQueryDTO dto) {
    IPage<GoodsVO> page = goodsService.queryPage(dto);
    return Result.success(page);
}
```

（另有 `GET /api/goods` 同功能端点在第 41-45 行，游客可访问）

#### 2.3.1 查询参数 — `dto/GoodsQueryDTO.java`

| 参数 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `categoryId` | Integer | null | 分类筛选 |
| `keyword` | String | null | 关键词搜索（标题+描述模糊匹配） |
| `status` | String | null | 商品状态（不传默认 ON_SALE） |
| `auditStatus` | String | null | 审核状态（不传默认 APPROVED） |
| `sellerId` | Long | null | 查某用户发布的商品 |
| `page` | Integer | 1 | 页码 |
| `size` | Integer | 10 | 每页条数 |
| `sort` | String | "newest" | 排序：newest / price_asc / price_desc |

#### 2.3.2 查询逻辑 — `service/impl/GoodsServiceImpl.java:94-143`

```java
public IPage<GoodsVO> queryPage(GoodsQueryDTO dto) {
    Page<Goods> page = Page.of(dto.getPage(), dto.getSize());
    LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();

    // 分类筛选
    if (dto.getCategoryId() != null) {
        wrapper.eq(Goods::getCategoryId, dto.getCategoryId());
    }

    // 关键词搜索（标题+描述 LIKE）
    if (dto.getKeyword() != null && !dto.getKeyword().isBlank()) {
        wrapper.and(w -> w
                .like(Goods::getTitle, dto.getKeyword())
                .or()
                .like(Goods::getDescription, dto.getKeyword()));
    }

    // 默认只查「在售」
    if (dto.getStatus() != null) {
        wrapper.eq(Goods::getStatus, dto.getStatus());
    } else {
        wrapper.eq(Goods::getStatus, ON_SALE);
    }

    // 前台默认只查「审核通过」
    if (dto.getAuditStatus() != null) {
        wrapper.eq(Goods::getAuditStatus, dto.getAuditStatus());
    } else {
        wrapper.eq(Goods::getAuditStatus, AUDIT_APPROVED);
    }

    // 排序
    switch (dto.getSort()) {
        case "price_asc"  -> wrapper.orderByAsc(Goods::getPrice);
        case "price_desc" -> wrapper.orderByDesc(Goods::getPrice);
        default            -> wrapper.orderByDesc(Goods::getCreatedAt);
    }

    return page(page, wrapper).convert(this::toGoodsVO);
}
```

核心过滤规则：
- **商品状态**：不传 → 只查 `ON_SALE`（在售）
- **审核状态**：不传 → 只查 `APPROVED`（已通过）
- **关键词搜索**：同时 LIKE 标题和描述（SQL: `WHERE title LIKE '%kw%' OR description LIKE '%kw%'`）

#### 2.3.3 认证排除 — `config/SaTokenConfig.java:18-28`

```java
registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
        .addPathPatterns("/api/**")
        .excludePathPatterns(
                "/api/auth/**",
                "/api/hello",
                "/api/categories",
                "/api/goods",
                "/api/goods/*"     // 同时匹配 /list, /123 等
        );
```

`/api/goods/*` 使用 Ant 风格单段通配符，同时覆盖：
- `/api/goods/list` — 列表别名
- `/api/goods/123` — 商品详情

`/api/goods/{id}/offline` 因涉及 `/api/**` 拦截，需要登录才能调用（符合"本人操作"鉴权要求）。

---

### 2.4 GET /api/goods/{id} — 商品详情

**入口：** `controller/GoodsController.java:55-60`

```java
@GetMapping("/{id}")
public Result<GoodsDetailVO> detail(@PathVariable Long id) {
    GoodsDetailVO vo = goodsService.getDetail(id);
    return Result.success(vo);
}
```

#### 2.4.1 查询逻辑 — `service/impl/GoodsServiceImpl.java:145-152`

```java
public GoodsDetailVO getDetail(Long goodsId) {
    Goods goods = getById(goodsId);
    if (goods == null) {
        throw new BusinessException(ResultCode.GOODS_NOT_FOUND);
    }
    return toDetailVO(goods);
}
```

#### 2.4.2 VO 组装（含发布者信息） — 同文件 233-258 行

```java
private GoodsDetailVO toDetailVO(Goods goods) {
    GoodsDetailVO vo = new GoodsDetailVO();
    vo.setId(goods.getId());
    vo.setTitle(goods.getTitle());
    vo.setDescription(goods.getDescription());
    vo.setRawDescription(goods.getRawDescription());
    vo.setPrice(goods.getPrice());
    vo.setOriginalPrice(goods.getOriginalPrice());
    vo.setCategoryId(goods.getCategoryId());
    vo.setCategoryName(goods.getCategoryName());
    vo.setImages(goodsImageService.getUrlsByGoodsId(goods.getId()));
    vo.setStatus(goods.getStatus());
    vo.setAuditStatus(goods.getAuditStatus());
    vo.setAuditRemark(goods.getAuditRemark());
    vo.setViewCount(goods.getViewCount());
    // ── 发布者信息 ──
    vo.setSellerId(goods.getSellerId());          // 卖家 ID
    vo.setSellerName(goods.getSellerName());      // 卖家用户名
    vo.setSellerContact(goods.getSellerContact());// 联系方式
    vo.setCreatedAt(goods.getCreatedAt());
    vo.setUpdatedAt(goods.getUpdatedAt());

    // 浏览量 +1（原子更新，异步无阻塞）
    incViewCount(goods.getId());
    return vo;
}
```

浏览量自增：

```java
public void incViewCount(Long goodsId) {
    lambdaUpdate()
            .setSql("view_count = view_count + 1")
            .eq(Goods::getId, goodsId)
            .update();
}
```

说明：使用 `SET view_count = view_count + 1` 的原子 SQL，避免并发丢失更新。

---

### 2.5 Day 8：关键词搜索、下架、AI 检测

#### 2.5.1 关键词搜索（列表接口内置）

已在 `GoodsServiceImpl.queryPage()` 中实现（见 2.3.2），请求示例：

```
GET /api/goods/list?keyword=iPhone&categoryId=1&sort=price_asc
```

生成 SQL：

```sql
WHERE status = 'ON_SALE'
  AND audit_status = 'APPROVED'
  AND category_id = 1
  AND (title LIKE '%iPhone%' OR description LIKE '%iPhone%')
ORDER BY price ASC
LIMIT 0, 10
```

#### 2.5.2 商品下架 — `PUT /api/goods/{id}/offline`

**入口：** `controller/GoodsController.java:72-79`

```java
@PutMapping("/{id}/offline")
public Result<Void> offline(@PathVariable Long id) {
    Long userId = StpUtil.getLoginIdAsLong();
    goodsService.updateStatus(id, "OFF_SHELF", userId);
    return Result.success();
}
```

**核心逻辑：** `service/impl/GoodsServiceImpl.java:154-174`

```java
public void updateStatus(Long goodsId, String status, Long userId) {
    Goods goods = getById(goodsId);
    if (goods == null) {
        throw new BusinessException(ResultCode.GOODS_NOT_FOUND);  // 404
    }
    if (!Objects.equals(goods.getSellerId(), userId)) {
        throw new BusinessException(ResultCode.FORBIDDEN);        // 403
    }
    if (!ON_SALE.equals(goods.getStatus())) {
        throw new BusinessException(400, "仅可对在售商品执行此操作"); // 400
    }
    if (!SOLD.equals(status) && !OFF_SHELF.equals(status)) {
        throw new BusinessException(400, "无效的状态值");            // 400
    }
    goods.setStatus(status);
    updateById(goods);
}
```

防越权校验链：

```
查商品 → 不存在 → 404
    ↓ 存在
sellerId == userId ? → 否 → 403（禁止操作他人商品）
    ↓ 是
当前状态 == ON_SALE ? → 否 → 400（只能下架在售商品）
    ↓ 是
setStatus("OFF_SHELF") → updateById
```

#### 2.5.3 AI 违规检测（发布流程内置）

已在 `GoodsServiceImpl.publish()` 第 62-67 行实现（见 2.1.2），流程：

```
标题 + 描述 → AiService.checkContent()
                  │
                  ▼
         DeepSeek API 审查
                  │
         ┌───────┴───────┐
         │ 无违规         │ 违规
         ▼                ▼
     继续入库        BusinessException(GOODS_VIOLATION)
                     → GlobalExceptionHandler
                     → Result.fail(2002, "内容违规: xxx")
```

- 调用 `AiService.checkContent(title, description)`
- 返回 `ContentCheckVO`，含 `violation:boolean` 和 `reason:String`
- 违规时直接抛业务异常，事务不会提交，goods 不会写入数据库

---

## 三、数据库表结构

### goods 表

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | 自增 |
| title | VARCHAR(100) | 标题 |
| description | TEXT | 商品描述 |
| raw_description | TEXT | 用户原始描述 |
| price | DECIMAL(10,2) | 售价 |
| original_price | DECIMAL(10,2) | 原价 |
| category_id | INT | 分类 ID |
| category_name | VARCHAR(32) | 分类名（冗余） |
| seller_id | BIGINT | 卖家 ID |
| seller_name | VARCHAR(32) | 卖家用户名（冗余） |
| seller_contact | VARCHAR(64) | 联系方式 |
| status | VARCHAR(16) | ON_SALE / SOLD / OFF_SHELF |
| audit_status | VARCHAR(16) | PENDING / APPROVED / REJECTED |
| audit_remark | VARCHAR(255) | 拒绝原因 |
| view_count | INT | 浏览次数 |
| deleted | TINYINT | 逻辑删除 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### goods_image 表

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | 自增 |
| goods_id | BIGINT | 关联 goods.id |
| url | VARCHAR(500) | 图片 URL |
| sort_order | INT | 排序（0=封面） |

---

## 四、商品状态流转

```
        publish()
            │
            ▼
      ┌─────────┐
      │ PENDING │  审核中（默认，仅管理员可见）
      └────┬────┘
           │ admin audit
     ┌─────┴─────┐
     ▼           ▼
┌─────────┐  ┌──────────┐
│APPROVED │  │ REJECTED │  已拒绝（不可见）
└────┬────┘  └──────────┘
     │ seller 操作
  ┌──┴──────────┐
  ▼             ▼
┌────┐    ┌──────────┐
│SOLD│    │ OFF_SHELF│  已下架
└────┘    └──────────┘
```

- **审核中** → 不展示到列表（列表默认 filter `audit_status = APPROVED`）
- **已通过 + 在售** → 列表可见
- **已售/已下架** → 只能由卖家本人操作

---

## 五、权限控制一览

| 接口 | 权限 |
|---|---|
| `GET /api/goods` / `/list` | 游客可访问 |
| `GET /api/goods/{id}` | 游客可访问 |
| `POST /api/goods` | 需登录 |
| `POST /api/upload/image` | 需登录 |
| `PUT /api/goods/{id}/status` | 需登录 + 本人操作 |
| `PUT /api/goods/{id}/offline` | 需登录 + 本人操作 |
| `PUT /api/goods/{id}/audit` | 需登录 + ADMIN 角色 |

游客范围由 `SaTokenConfig.excludePathPatterns` 控制，业务鉴权（本人操作、ADMIN 角色）在 `GoodsServiceImpl` 中编程实现。

---

## 六、错误码

| code | 含义 |
|---|---|
| 2002 | 商品内容违规 |
| 2001 | 商品不存在 |
| 403 | 越权操作（非本人/非管理员） |
| 400 | 参数错误/状态非法 |
| 3001 | AI 服务异常（非阻塞，按通过处理） |
