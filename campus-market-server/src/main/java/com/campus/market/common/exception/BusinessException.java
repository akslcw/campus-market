package com.campus.market.common.exception;

import com.campus.market.common.ResultCode;
import lombok.Getter;

// 业务异常类，包含错误码和消息
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(ResultCode rc) {
        super(rc.getMsg());
        this.code = rc.getCode();
    }

    public BusinessException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }
}