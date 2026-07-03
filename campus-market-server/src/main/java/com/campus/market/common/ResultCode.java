package com.campus.market.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 错误码枚举
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "成功"),

    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),

    USERNAME_EXISTS(1001, "用户名已存在"),
    USER_NOT_FOUND(1002, "用户不存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    USER_BANNED(1004, "账号已被封禁"),

    GOODS_NOT_FOUND(2001, "商品不存在"),
    GOODS_VIOLATION(2002, "商品内容违规"),

    AI_SERVICE_ERROR(3001, "AI 服务暂不可用"),

    SYSTEM_ERROR(500, "系统异常,请稍后重试");

    private final Integer code;
    private final String msg;
}