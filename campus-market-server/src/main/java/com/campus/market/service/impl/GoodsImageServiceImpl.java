package com.campus.market.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.market.entity.GoodsImage;
import com.campus.market.mapper.GoodsImageMapper;
import com.campus.market.service.GoodsImageService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 成员二
 */
@Service
public class GoodsImageServiceImpl extends ServiceImpl<GoodsImageMapper, GoodsImage> implements GoodsImageService {

    @Override
    public void saveImages(Long goodsId, List<String> urls) {
        if (urls == null || urls.isEmpty()) return;
        for (int i = 0; i < urls.size(); i++) {
            GoodsImage img = new GoodsImage();
            img.setGoodsId(goodsId);
            img.setUrl(urls.get(i));
            img.setSortOrder(i);
            save(img);
        }
    }

    @Override
    public List<String> getUrlsByGoodsId(Long goodsId) {
        List<GoodsImage> images = lambdaQuery()
                .eq(GoodsImage::getGoodsId, goodsId)
                .orderByAsc(GoodsImage::getSortOrder)
                .list();
        return images.stream()
                .map(GoodsImage::getUrl)
                .toList();
    }

    @Override
    public String getCoverUrl(Long goodsId) {
        List<GoodsImage> images = lambdaQuery()
                .eq(GoodsImage::getGoodsId, goodsId)
                .orderByAsc(GoodsImage::getSortOrder)
                .last("LIMIT 1")
                .list();
        return images.isEmpty() ? null : images.get(0).getUrl();
    }
}
