package com.campus.market.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.common.Result;
import com.campus.market.dto.GoodsAuditDTO;
import com.campus.market.dto.GoodsQueryDTO;
import com.campus.market.entity.User;
import com.campus.market.service.GoodsService;
import com.campus.market.service.UserService;
import com.campus.market.vo.GoodsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.campus.market.common.ResultCode.USER_NOT_FOUND;


@Tag(name = "管理员模块", description = "商品审核/用户管理")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;
    private final GoodsService goodsService;

    @Operation(summary = "IF-15 待审核商品列表", description = "管理员查看所有待审核(PENDING)商品，分页")
    @GetMapping("/goods/pending")
    @SaCheckLogin
    @SaCheckRole("ADMIN")
    public Result<IPage<GoodsVO>> pendingGoods(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        GoodsQueryDTO dto = new GoodsQueryDTO();
        dto.setAuditStatus("PENDING");
        dto.setPage(page);
        dto.setSize(size);
        IPage<GoodsVO> result = goodsService.queryPage(dto);
        return Result.success(result);
    }

    @Operation(summary = "IF-16 审核商品通过", description = "管理员审核通过商品")
    @PutMapping("/goods/{id}/approve")
    @SaCheckLogin
    @SaCheckRole("ADMIN")
    public Result<Void> approveGoods(@PathVariable Long id) {
        GoodsAuditDTO dto = new GoodsAuditDTO();
        dto.setAuditStatus("APPROVED");
        goodsService.audit(id, dto);
        return Result.success();
    }
    @Operation(summary = "IF-16 审核商品拒绝", description = "管理员拒绝商品，必填原因")
    @PutMapping("/goods/{id}/reject")
    @SaCheckLogin
    @SaCheckRole("ADMIN")
    public Result<Void> rejectGoods(@PathVariable Long id, @RequestBody RejectReq req) {
        GoodsAuditDTO dto = new GoodsAuditDTO();
        dto.setAuditStatus("REJECTED");
        dto.setAuditRemark(req.getReason());
        goodsService.audit(id, dto);
        return Result.success();
    }

    @Operation(summary = "IF-13 用户列表", description = "管理员查看所有用户")
    @GetMapping("/users")
    @SaCheckLogin
    @SaCheckRole("ADMIN")
    public Result<Page<User>> userList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<User> pageResult = userService.lambdaQuery()
                .eq(User::getRole, "USER")
                .page(new Page<>(page, size));
        pageResult.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(pageResult);
    }

    @Operation(summary = "IF-14 封禁用户", description = "管理员封禁用户账号")
    @PutMapping("/users/{id}/ban")
    @SaCheckLogin
    @SaCheckRole("ADMIN")
    public Result<Void> banUser(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.fail(USER_NOT_FOUND);
        }
        user.setStatus(1);
        userService.updateById(user);
        return Result.success();
    }

    @Operation(summary = "IF-14 解封用户", description = "管理员解封用户账号")
    @PutMapping("/users/{id}/unban")
    @SaCheckLogin
    @SaCheckRole("ADMIN")
    public Result<Void> unbanUser(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.fail(USER_NOT_FOUND);
        }
        user.setStatus(0);
        userService.updateById(user);
        return Result.success();
    }

    @Data
    @Schema(description = "审核拒绝请求")
    public static class RejectReq {
        @Schema(description = "拒绝原因", example = "图片与描述不符")
        private String reason;
    }
}
