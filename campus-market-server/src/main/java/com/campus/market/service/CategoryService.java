package com.campus.market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.market.entity.Category;
import java.util.List;

/**
 * @author 成员二
 */
public interface CategoryService extends IService<Category> {
    /** 获取所有分类（按 sort_order 排序） */
    List<Category> listAll();
}
