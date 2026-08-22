package com.mystikos.common.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一响应结果封装。
 *
 * @param <T> 业务数据类型
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@Schema(description = "统一响应结果封装")
public class APIResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private IResponseCode responseCode;

    /**
     * 状态码
     */
    @Schema(description = "状态码，200 表示成功，其余见各上下文错误码约定")
    private int code;

    /**
     * 消息
     */
    @Schema(description = "消息")
    private String message;

    /**
     * 数据
     */
    @Schema(description = "数据")
    private T data;

    public APIResponse(IResponseCode responseCode, T data) {
        this.responseCode = responseCode;
        this.code = responseCode.getCode();
        this.message = responseCode.getMessage();
        this.data = data;
    }

    public static <T> APIResponse<T> ok() {
        return build(ResponseCode.SUCCESS, null, null);
    }

    public static <T> APIResponse<T> ok(T data) {
        return build(ResponseCode.SUCCESS, data, null);
    }

    public static <T> APIResponse<T> ok(T data, String message) {
        return build(ResponseCode.SUCCESS, data, message);
    }

    public static <T> APIResponse<T> failed() {
        return build(ResponseCode.FAILED, null, null);
    }

    public static <T> APIResponse<T> failed(String message) {
        return build(ResponseCode.FAILED, null, message);
    }

    public static <T> APIResponse<T> failed(IResponseCode responseCode) {
        return build(responseCode, null, null);
    }

    /**
     * 固定错误码 + 动态消息：枚举给出可枚举、可被前端/网关识别的业务码，
     * 消息按运行时上下文拼接（例如"课程名称 'xxx' 已存在"）。
     */
    public static <T> APIResponse<T> failed(IResponseCode responseCode, String message) {
        return build(responseCode, null, message);
    }

    private static <T> APIResponse<T> build(IResponseCode responseCode, T data, String message) {
        APIResponse<T> response = new APIResponse<>();
        response.responseCode = responseCode;
        response.code = responseCode.getCode();
        response.message = message != null ? message : responseCode.getMessage();
        response.data = data;
        return response;
    }
}
