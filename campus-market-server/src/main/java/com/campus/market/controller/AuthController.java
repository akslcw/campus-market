package com.campus.market.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.campus.market.common.Result;
import com.campus.market.common.ResultCode;
import com.campus.market.common.exception.BusinessException;
import com.campus.market.dto.LoginDTO;
import com.campus.market.dto.RegisterDTO;
import com.campus.market.entity.User;
import com.campus.market.service.UserService;
import com.campus.market.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;

/**
 * 认证接口（成员一/成员三）
 */
@Tag(name = "认证模块", description = "注册/登录/退出")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "IF-01 用户注册", description = "新用户注册，密码BCrypt加密，默认角色USER")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        String username = normalizeUsername(dto.getUsername());
        if (userService.findByUsername(username) != null) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        user.setNickname(dto.getNickname() == null ? username : dto.getNickname());
        user.setRole("USER");
        user.setStatus(0);
        try {
            userService.save(user);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
        return Result.success();
    }

    @Operation(summary = "IF-02 用户登录", description = "校验用户名密码，返回JWT Token和用户信息")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        User user = userService.findByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (!BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new BusinessException(ResultCode.USER_BANNED);
        }

        StpUtil.login(user.getId());
        StpUtil.getSession().set("role", user.getRole());

        LoginVO vo = LoginVO.builder()
                .token(StpUtil.getTokenValue())
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .role(user.getRole())
                .build();
        return Result.success(vo);
    }

    @Operation(summary = "IF-03 退出登录", description = "注销当前Token")
    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success();
    }

    @Operation(summary = "获取当前用户ID", description = "从Token中解析当前登录用户ID")
    @GetMapping("/me")
    public Result<Long> me() {
        return Result.success(StpUtil.getLoginIdAsLong());
    }

    private String normalizeUsername(String username) {
        String normalized = username.trim();
        if (normalized.length() < 3 || normalized.length() > 20) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户名长度需为 3-20 位");
        }
        return normalized;
    }
}
