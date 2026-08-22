package com.mystikos.common.web.exception;

import com.mystikos.common.result.IResponseCode;
import lombok.Getter;

/**
 * 业务异常基类。各限界上下文可直接抛出，或派生出自己的异常类型
 * （如 BookingException extends BusinessException）并附带静态工厂方法，
 * 用本模块自己的 {@link IResponseCode} 枚举实现携带错误码。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final IResponseCode resultCode;

    public BusinessException(String message) {
        super(message);
        this.resultCode = null;
    }

    public BusinessException(IResponseCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    /**
     * 固定错误码 + 动态消息。
     */
    public BusinessException(IResponseCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.resultCode = null;
    }
}
