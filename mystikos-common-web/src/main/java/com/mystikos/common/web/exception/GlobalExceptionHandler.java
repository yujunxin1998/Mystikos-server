package com.mystikos.common.web.exception;

import com.mystikos.common.result.APIResponse;
import com.mystikos.common.result.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器。业务模块如需处理本模块特有的异常类型（如某个第三方 SDK
 * 抛出的受检异常），可以在自己的 adapter/web 包下追加一个
 * {@code @RestControllerAdvice(basePackages = "com.mystikos.xxx")} 的处理器，
 * 与这里的全局兜底并存，不冲突。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 参数校验异常（{@code @Valid} / {@code @Validated} 触发）
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public APIResponse<Void> handleValidException(Exception e) {
        BindingResult bindingResult = e instanceof MethodArgumentNotValidException manv
                ? manv.getBindingResult()
                : ((BindException) e).getBindingResult();

        FieldError fieldError = bindingResult.getFieldError();
        String message = fieldError != null
                ? fieldError.getField() + " " + fieldError.getDefaultMessage()
                : ResponseCode.VALIDATE_FAILED.getMessage();

        log.warn("参数校验失败：{}", message);
        return APIResponse.failed(ResponseCode.VALIDATE_FAILED, message);
    }

    /**
     * 业务异常。resultCode 为空时（直接 new BusinessException(message) 的场景）
     * 归类为通用 BUSINESS_ERROR，但保留调用方传入的具体消息。
     */
    @ExceptionHandler(BusinessException.class)
    public APIResponse<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常：{}", e.getMessage());
        return e.getResultCode() != null
                ? APIResponse.failed(e.getResultCode(), e.getMessage())
                : APIResponse.failed(ResponseCode.BUSINESS_ERROR, e.getMessage());
    }

    /** Multipart 请求在进入 Controller 前超过 Spring 配置的上传上限。 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public APIResponse<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("上传文件超过允许大小：{}", e.getMessage());
        return APIResponse.failed(ResponseCode.FILE_TOO_LARGE);
    }

    /**
     * 静态资源不存在（典型场景：浏览器打开 /doc.html 时自动带一个 favicon.ico 请求）。
     * 这是正常情况，不是系统异常，不走 APIResponse 信封、不打 ERROR 日志，
     * 否则会把日志刷满没意义的堆栈，掩盖真正的错误。
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNoResourceFound(NoResourceFoundException e) {
        log.debug("静态资源不存在：{}", e.getResourcePath());
    }

    /**
     * 未分类的系统异常，不向前端暴露堆栈细节。
     */
    @ExceptionHandler(Exception.class)
    public APIResponse<Void> handleException(Exception e) {
        log.error("系统内部异常", e);
        return APIResponse.failed(ResponseCode.FAILED);
    }
}
