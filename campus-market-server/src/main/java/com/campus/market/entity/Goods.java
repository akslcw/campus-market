package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品主表实体
 * @author 成员二
 */
@Data
@TableName("goods")
public class Goods {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String description;
    private String rawDescription;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer categoryId;
    private String categoryName;
    private Long sellerId;
    private String sellerName;
    private String sellerContact;

    /** 商品状态: ON_SALE / SOLD / OFF_SHELF */
    private String status;

    /** 审核状态: PENDING / APPROVED / REJECTED */
    private String auditStatus;

    /** 审核拒绝原因 */
    private String auditRemark;

    private Integer viewCount;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
