package com.campus.market.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.market.common.Result;
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

@Tag(name = "用户模块", description = "个人中心/个人信息")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final GoodsService goodsService;

    @Operation(summary = "IF-11 获取个人信息", description = "查看当前登录用户的详细资料")
    @GetMapping("/profile")
    @SaCheckLogin
    public Result<UserProfileVO> getProfile() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getById(userId);
        if (user == null) {
            return Result.fail(USER_NOT_FOUND);
        }
        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());

        vo.setAvatar(user.getAvatar());
        vo.setContact(user.getContact());
        vo.setRole(user.getRole());
        return Result.success(vo);
    }

    @Operation(summary = "IF-12 修改个人信息", description = "修改昵称/头像/联系方式")
    @PutMapping("/profile")
    @SaCheckLogin
    public Result<Void> updateProfile(@RequestBody UpdateProfileDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        User existingUser = userService.getById(userId);
        if (existingUser == null) {
            return Result.fail(USER_NOT_FOUND);
        }
        existingUser.setNickname(dto.getNickname());
        existingUser.setAvatar(dto.getAvatar());
        existingUser.setContact(dto.getContact());
        userService.updateById(existingUser);
        return Result.success();
    }

    @Operation(summary = "IF-17 我的发布", description = "查看当前用户发布的商品列表（全部状态），可选 status 筛选")
    @GetMapping("/goods")
    @SaCheckLogin
    public Result<IPage<GoodsVO>> myGoods(
            @Parameter(description = "商品状态筛选：ON_SALE/SOLD/OFF_SHELF，不传则查全部") @RequestParam(required = false) String status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        Long userId = StpUtil.getLoginIdAsLong();
        GoodsQueryDTO dto = new GoodsQueryDTO();
        dto.setSellerId(userId);
        dto.setStatus(status);
        dto.setPage(page);
        dto.setSize(size);
        IPage<GoodsVO> result = goodsService.queryPage(dto);
        return Result.success(result);
    }

    @Data
    @Schema(description = "修改个人信息请求")
    public static class UpdateProfileDTO {
        @Schema(description = "昵称", example = "小明")
        private String nickname;
        @Schema(description = "头像URL", example = "/static/avatar.jpg")
        private String avatar;
        @Schema(description = "联系方式", example = "QQ:123456")
        private String contact;
    }

    @Data
    @Schema(description = "用户资料")
    public static class UserProfileVO {
        @Schema(description = "用户ID")
        private Long id;
        @Schema(description = "用户名")
        private String username;
        @Schema(description = "昵称")
        private String nickname;
        @Schema(description = "头像URL")
        private String avatar;
        @Schema(description = "联系方式")
        private String contact;
        @Schema(description = "角色：USER/ADMIN")
        private String role;
    }
}
