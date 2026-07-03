package com.campus.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商品列表查询请求
 * @author 成员二
 */
@Data
@Schema(description = "商品列表查询参数")
public class GoodsQueryDTO {

    @Schema(description = "分类ID", example = "1")
    private Integer categoryId;

    @Schema(description = "关键词搜索（匹配标题和描述）", example = "iPhone")
    private String keyword;

    @Schema(description = "商品状态", example = "ON_SALE")
    private String status;

    @Schema(description = "审核状态", example = "APPROVED")
    private String auditStatus;

    @Schema(description = "卖家ID（查某个用户的发布列表）", example = "4")
    private Long sellerId;

    @Schema(description = "页码（从1开始）", example = "1")
    private Integer page = 1;

    @Schema(description = "每页条数", example = "10")
    private Integer size = 10;

    @Schema(description = "排序：newest / price_asc / price_desc", example = "newest")
    private String sort = "newest";
}
