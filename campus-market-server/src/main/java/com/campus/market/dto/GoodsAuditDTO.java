package com.campus.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 商品审核请求
 * @author 成员二
 */
@Data
@Schema(description = "商品审核操作")
public class GoodsAuditDTO {

    @Schema(description = "审核结果：APPROVED / REJECTED", example = "APPROVED")
    @NotBlank(message = "审核结果不能为空")
    private String auditStatus;

    @Schema(description = "拒绝原因（拒绝时必填）", example = "图片与描述不符")
    private String auditRemark;
}
