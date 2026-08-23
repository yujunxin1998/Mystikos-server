package com.mystikos.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 全局通用响应码。业务限界上下文不要往这里加码，应在自己模块内定义实现
 * {@link IResponseCode} 的枚举，并使用为该模块分配的号段（见
 * docs/architecture/exception-handling.md）。
 */
@Getter
@AllArgsConstructor
public enum ResponseCode implements IResponseCode {

    SUCCESS(200, "success"),
    FAILED(500, "服务器内部错误"),
    VALIDATE_FAILED(400, "参数校验失败"),
    UNAUTHORIZED(401, "暂未登录或登录已过期"),
    FORBIDDEN(403, "没有相关权限"),
    NOT_FOUND(404, "请求的资源不存在"),

    PARAM_ERROR(1001, "参数错误"),
    PARAM_MISSING(1002, "缺少必要参数"),
    BUSINESS_ERROR(1003, "业务处理异常"),
    DATA_NOT_EXIST(1004, "数据不存在"),
    DATA_CONFLICT(1005, "数据已存在或存在冲突"),

    FILE_EMPTY(1006, "上传文件为空"),
    FILE_UPLOAD_FAILED(1007, "文件上传失败"),
    FILE_NOT_FOUND(1008, "文件不存在"),
    FILE_TOO_LARGE(1009, "上传文件不能超过 5 MB");

    private final int code;
    private final String message;
}
