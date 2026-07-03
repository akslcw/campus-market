package com.campus.market.controller;

import com.campus.market.common.Result;
import com.campus.market.entity.Category;
import com.campus.market.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类接口
 * @author 成员二
 */
@Tag(name = "分类模块", description = "获取商品分类列表")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "获取分类列表", description = "返回所有启用的分类，按排序权重升序，游客可访问")
    @GetMapping
    public Result<List<Category>> list() {
        return Result.success(categoryService.listAll());
    }
}
