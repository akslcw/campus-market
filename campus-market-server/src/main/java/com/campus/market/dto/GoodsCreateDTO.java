package com.campus.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品发布请求
 * @author 成员二
 */
@Data
@Schema(description = "商品发布请求")
public class GoodsCreateDTO {

    @Schema(description = "商品标题", example = "二手 iPhone 14 128G")
    @NotBlank(message = "标题不能为空")
    @Size(min = 2, max = 100, message = "标题长度需为 2-100 位")
    private String title;

    @Schema(description = "商品描述（建议AI优化后）", example = "出一台自用iPhone14，使用一年，轻微划痕")
    @NotBlank(message = "描述不能为空")
    private String description;

    @Schema(description = "用户原始描述")
    private String rawDescription;

    @Schema(description = "售价", example = "3500.00")
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于 0")
    @DecimalMax(value = "99999.99", message = "价格不能超过 99999.99")
    private BigDecimal price;

    @Schema(description = "原价", example = "5999.00")
    private BigDecimal originalPrice;

    @Schema(description = "分类ID", example = "1")
    @NotNull(message = "分类不能为空")
    private Integer categoryId;

    @Schema(description = "分类名称", example = "数码电子")
    @NotBlank(message = "分类名称不能为空")
    private String categoryName;

    @Schema(description = "图片URL列表（先调上传接口获得）", example = "[\"/static/202606/21/abc.jpg\"]")
    @NotEmpty(message = "请至少上传 1 张图片")
    @Size(min = 1, max = 5, message = "图片数量需为 1-5 张")
    private List<String> images;

    @Schema(description = "联系方式（不填则使用用户资料中的）", example = "QQ:123456")
    private String contact;
}
