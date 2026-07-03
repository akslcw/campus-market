package com.campus.market.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.market.common.Result;
import com.campus.market.dto.GoodsCreateDTO;
import com.campus.market.dto.GoodsQueryDTO;
import com.campus.market.dto.GoodsStatusDTO;
import com.campus.market.service.GoodsService;
import com.campus.market.vo.GoodsDetailVO;
import com.campus.market.vo.GoodsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商品接口 — IF-04 / IF-05 / IF-06 / IF-07
 * @author 成员二
 */
@Tag(name = "商品模块", description = "IF-04 列表 / IF-05 详情 / IF-06 发布 / IF-07 状态")
@RestController
@RequestMapping("/api/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService;

    @Operation(summary = "IF-06 发布商品",
            description = "发布新品并自动触发AI违规检测：正常内容自动通过，AI异常时转人工审核")
    @PostMapping
    public Result<GoodsDetailVO> publish(@Valid @RequestBody GoodsCreateDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        GoodsDetailVO vo = goodsService.publish(dto, userId);
        return Result.success(vo);
    }

    @Operation(summary = "IF-04 商品列表", description = "分页列表 + 分类筛选 + 关键词搜索 + 排序，游客可访问")
    @GetMapping
    public Result<IPage<GoodsVO>> list(GoodsQueryDTO dto) {
        IPage<GoodsVO> page = goodsService.queryPage(dto);
        return Result.success(page);
    }

    @Operation(summary = "IF-05 商品详情", description = "查看完整商品信息(描述/图片/卖家/联系方式)，自动+1浏览量")
    @GetMapping("/{id}")
    public Result<GoodsDetailVO> detail(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        GoodsDetailVO vo = goodsService.getDetail(id);
        return Result.success(vo);
    }

    @Operation(summary = "IF-07 商品状态流转(选做)", description = "卖家标记已售(SOLD)或下架(OFF_SHELF)，仅本人操作")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @Parameter(description = "商品ID") @PathVariable Long id,
            @Valid @RequestBody GoodsStatusDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        goodsService.updateStatus(id, dto.getStatus(), userId);
        return Result.success();
    }

    @Operation(summary = "商品下架", description = "卖家下架自己的商品，仅本人操作")
    @PutMapping("/{id}/offline")
    public Result<Void> offline(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        goodsService.updateStatus(id, "OFF_SHELF", userId);
        return Result.success();
    }
}
