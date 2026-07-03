package com.campus.market.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.market.entity.Category;
import com.campus.market.mapper.CategoryMapper;
import com.campus.market.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 成员二
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public List<Category> listAll() {
        return lambdaQuery()
                .orderByAsc(Category::getSortOrder)
                .list();
    }
}
