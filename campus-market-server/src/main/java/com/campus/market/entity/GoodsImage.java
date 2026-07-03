package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 商品图片表实体
 * @author 成员二
 */
@Data
@TableName("goods_image")
public class GoodsImage {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long goodsId;
    private String url;
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
