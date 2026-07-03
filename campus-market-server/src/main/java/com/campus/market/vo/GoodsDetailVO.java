package com.campus.market.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品详情 VO
 * @author 成员二
 */
@Data
@Schema(description = "商品详情")
public class GoodsDetailVO {
    @Schema(description = "商品ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "用户原始描述")
    private String rawDescription;

    @Schema(description = "售价")
    private BigDecimal price;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "分类ID")
    private Integer categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "所有图片URL")
    private List<String> images;

    @Schema(description = "商品状态：ON_SALE / SOLD / OFF_SHELF")
    private String status;

    @Schema(description = "审核状态：PENDING / APPROVED / REJECTED")
    private String auditStatus;

    @Schema(description = "审核拒绝原因")
    private String auditRemark;

    @Schema(description = "浏览次数")
    private Integer viewCount;

    @Schema(description = "卖家ID")
    private Long sellerId;

    @Schema(description = "卖家用户名")
    private String sellerName;

    @Schema(description = "联系方式")
    private String sellerContact;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
