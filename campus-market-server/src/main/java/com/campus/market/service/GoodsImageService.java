package com.campus.market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.market.entity.GoodsImage;
import java.util.List;

/**
 * @author 成员二
 */
public interface GoodsImageService extends IService<GoodsImage> {
    /** 保存商品图片 */
    void saveImages(Long goodsId, List<String> urls);

    /** 获取商品所有图片（按 sort_order 排序） */
    List<String> getUrlsByGoodsId(Long goodsId);

    /** 获取商品首张图 */
    String getCoverUrl(Long goodsId);
}
