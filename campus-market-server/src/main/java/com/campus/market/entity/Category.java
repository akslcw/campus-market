package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 分类字典表实体
 * @author 成员二
 */
@Data
@TableName("category")
@Schema(description = "商品分类")
public class Category {
    @TableId(type = IdType.AUTO)
    @Schema(description = "分类ID")
    private Integer id;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "排序权重")
    private Integer sortOrder;

    @TableLogic
    @Schema(description = "逻辑删除")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
