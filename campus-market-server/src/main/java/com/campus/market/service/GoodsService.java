package com.campus.market.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.market.dto.GoodsAuditDTO;
import com.campus.market.dto.GoodsCreateDTO;
import com.campus.market.dto.GoodsQueryDTO;
import com.campus.market.entity.Goods;
import com.campus.market.vo.GoodsDetailVO;
import com.campus.market.vo.GoodsVO;

/**
 * @author 成员二
 */
public interface GoodsService extends IService<Goods> {
    /** 发布商品 */
    GoodsDetailVO publish(GoodsCreateDTO dto, Long userId);

    /** 查询商品列表（分页 + 筛选 + 搜索 + 排序） */
    IPage<GoodsVO> queryPage(GoodsQueryDTO dto);

    /** 查看商品详情 */
    GoodsDetailVO getDetail(Long goodsId);

    /** 卖家修改商品状态 */
    void updateStatus(Long goodsId, String status, Long userId);

    /** 管理员审核商品 */
    void audit(Long goodsId, GoodsAuditDTO dto);

    /** 增加浏览次数 */
    void incViewCount(Long goodsId);
}
