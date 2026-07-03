package com.campus.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 商品状态变更请求
 * @author 成员二
 */
@Data
@Schema(description = "商品状态变更")
public class GoodsStatusDTO {

    @Schema(description = "目标状态：SOLD / OFF_SHELF", example = "SOLD")
    @NotBlank(message = "状态不能为空")
    private String status;
}
