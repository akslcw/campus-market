package com.campus.market.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.market.common.ResultCode;
import com.campus.market.common.exception.BusinessException;
import com.campus.market.dto.GoodsAuditDTO;
import com.campus.market.dto.GoodsCreateDTO;
import com.campus.market.dto.GoodsQueryDTO;
import com.campus.market.entity.Goods;
import com.campus.market.entity.User;
import com.campus.market.mapper.GoodsMapper;
import com.campus.market.service.GoodsImageService;
import com.campus.market.service.GoodsService;
import com.campus.market.service.UserService;
import com.campus.market.service.ai.AiService;
import com.campus.market.vo.ContentCheckVO;
import com.campus.market.vo.GoodsDetailVO;
import com.campus.market.vo.GoodsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 商品服务实现
 * @author 成员二
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    private final GoodsImageService goodsImageService;
    private final UserService userService;
    private final AiService aiService;

    // -- 商品状态常量 --
    public static final String ON_SALE = "ON_SALE";
    public static final String SOLD = "SOLD";
    public static final String OFF_SHELF = "OFF_SHELF";

    // -- 审核状态常量 --
    public static final String AUDIT_PENDING = "PENDING";
    public static final String AUDIT_APPROVED = "APPROVED";
    public static final String AUDIT_REJECTED = "REJECTED";

    @Override
    @Transactional
    public GoodsDetailVO publish(GoodsCreateDTO dto, Long userId) {
        User seller = userService.getById(userId);
        if (seller == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // IF-06: 发布前触发 AI 违规检测
        ContentCheckVO checkResult = aiService.checkContent(dto.getTitle(), dto.getDescription());
        if (Boolean.TRUE.equals(checkResult.getViolation())) {
            throw new BusinessException(ResultCode.GOODS_VIOLATION.getCode(),
                    "内容违规: " + checkResult.getReason());
        }

        // 1. 创建商品
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
        goods.setStatus(ON_SALE);
        goods.setAuditStatus(checkResult.isReviewCompleted()
                ? AUDIT_APPROVED
                : AUDIT_PENDING);
        goods.setViewCount(0);
        save(goods);

        // 2. 保存图片
        goodsImageService.saveImages(goods.getId(), dto.getImages());

        log.info("商品发布成功: id={}, title={}, seller={}", goods.getId(), goods.getTitle(), userId);
        return getDetail(goods.getId());
    }

    @Override
    public IPage<GoodsVO> queryPage(GoodsQueryDTO dto) {
        Page<Goods> page = Page.of(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();

        // 分类筛选
        if (dto.getCategoryId() != null) {
            wrapper.eq(Goods::getCategoryId, dto.getCategoryId());
        }

        // 关键词搜索（匹配标题和描述）
        if (dto.getKeyword() != null && !dto.getKeyword().isBlank()) {
            wrapper.and(w -> w
                    .like(Goods::getTitle, dto.getKeyword())
                    .or()
                    .like(Goods::getDescription, dto.getKeyword()));
        }

        // 指定卖家（“我的发布”场景：查自己所有状态的商品）
        boolean ownerQuery = dto.getSellerId() != null;
        if (ownerQuery) {
            wrapper.eq(Goods::getSellerId, dto.getSellerId());
        }

        // 商品状态：显式传入则按其过滤；否则前台默认只查在售，查自己的发布不强制过滤
        if (dto.getStatus() != null) {
            wrapper.eq(Goods::getStatus, dto.getStatus());
        } else if (!ownerQuery) {
            wrapper.eq(Goods::getStatus, ON_SALE);
        }

        // 审核状态：显式传入则按其过滤（如待审核列表 PENDING）；否则前台默认只查已通过，查自己的发布不强制过滤
        if (dto.getAuditStatus() != null) {
            wrapper.eq(Goods::getAuditStatus, dto.getAuditStatus());
        } else if (!ownerQuery) {
            wrapper.eq(Goods::getAuditStatus, AUDIT_APPROVED);
        }

        // 排序
        switch (dto.getSort()) {
            case "price_asc"  -> wrapper.orderByAsc(Goods::getPrice);
            case "price_desc" -> wrapper.orderByDesc(Goods::getPrice);
            default            -> wrapper.orderByDesc(Goods::getCreatedAt);
        }

        IPage<Goods> goodsPage = page(page, wrapper);

        // 转换为 VO
        return goodsPage.convert(this::toGoodsVO);
    }

    @Override
    public GoodsDetailVO getDetail(Long goodsId) {
        Goods goods = getById(goodsId);
        if (goods == null) {
            throw new BusinessException(ResultCode.GOODS_NOT_FOUND);
        }
        return toDetailVO(goods);
    }

    @Override
    public void updateStatus(Long goodsId, String status, Long userId) {
        Goods goods = getById(goodsId);
        if (goods == null) {
            throw new BusinessException(ResultCode.GOODS_NOT_FOUND);
        }
        // 只能操作自己的商品
        if (!Objects.equals(goods.getSellerId(), userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        // 状态校验
        if (!ON_SALE.equals(goods.getStatus())) {
            throw new BusinessException(400, "仅可对在售商品执行此操作");
        }
        if (!SOLD.equals(status) && !OFF_SHELF.equals(status)) {
            throw new BusinessException(400, "无效的状态值");
        }

        goods.setStatus(status);
        updateById(goods);
    }

    @Override
    public void audit(Long goodsId, GoodsAuditDTO dto) {
        // 鉴权：仅管理员可审核
        String role = (String) StpUtil.getSession().get("role");
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        Goods goods = getById(goodsId);
        if (goods == null) {
            throw new BusinessException(ResultCode.GOODS_NOT_FOUND);
        }
        if (!AUDIT_PENDING.equals(goods.getAuditStatus())) {
            throw new BusinessException(400, "该商品已审核过");
        }
        if (!AUDIT_APPROVED.equals(dto.getAuditStatus())
                && !AUDIT_REJECTED.equals(dto.getAuditStatus())) {
            throw new BusinessException(400, "审核操作无效");
        }
        if (AUDIT_REJECTED.equals(dto.getAuditStatus())
                && (dto.getAuditRemark() == null || dto.getAuditRemark().isBlank())) {
            throw new BusinessException(400, "拒绝时必须填写原因");
        }

        goods.setAuditStatus(dto.getAuditStatus());
        goods.setAuditRemark(dto.getAuditRemark());
        updateById(goods);

        log.info("商品审核完成: id={}, auditStatus={}", goodsId, dto.getAuditStatus());
    }

    @Override
    public void incViewCount(Long goodsId) {
        lambdaUpdate()
                .setSql("view_count = view_count + 1")
                .eq(Goods::getId, goodsId)
                .update();
    }

    // -- VO 转换 --

    private GoodsVO toGoodsVO(Goods goods) {
        GoodsVO vo = new GoodsVO();
        vo.setId(goods.getId());
        vo.setTitle(goods.getTitle());
        vo.setPrice(goods.getPrice());
        vo.setOriginalPrice(goods.getOriginalPrice());
        vo.setCategoryId(goods.getCategoryId());
        vo.setCategoryName(goods.getCategoryName());
        vo.setStatus(goods.getStatus());
        vo.setAuditStatus(goods.getAuditStatus());
        vo.setViewCount(goods.getViewCount());
        vo.setCoverImage(goodsImageService.getCoverUrl(goods.getId()));
        vo.setSellerName(goods.getSellerName());
        vo.setCreatedAt(goods.getCreatedAt());
        return vo;
    }

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
        vo.setSellerId(goods.getSellerId());
        vo.setSellerName(goods.getSellerName());
        vo.setSellerContact(goods.getSellerContact());
        vo.setCreatedAt(goods.getCreatedAt());
        vo.setUpdatedAt(goods.getUpdatedAt());

        // 浏览次数异步更新（不影响返回）
        incViewCount(goods.getId());

        return vo;
    }
}
