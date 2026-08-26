package com.mystikos.systemoperation.infrastructure.web;

import com.mystikos.common.security.CurrentUserContext;
import com.mystikos.systemoperation.application.service.OperationLogApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;

/**
 * 记录 {@code /api/v1/manage/**} 下非 GET 请求的操作留痕。用 HandlerInterceptor 而不是纯 Filter，
 * 是因为 DispatcherServlet（连带 Interceptor）在 Spring Security 过滤器链之后才执行，
 * {@code afterCompletion} 里能稳拿到 SecurityContext，不用处理 Filter 和 Spring Security
 * 过滤器顺序的坑。注册范围见 WebMvcConfig。
 */
public class OperationLogInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(OperationLogInterceptor.class);
    private static final String START_TIME_ATTRIBUTE = OperationLogInterceptor.class.getName() + ".startTime";
    private static final int MAX_BODY_LENGTH = 2000;

    private final OperationLogApplicationService operationLogApplicationService;

    public OperationLogInterceptor(OperationLogApplicationService operationLogApplicationService) {
        this.operationLogApplicationService = operationLogApplicationService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                 Exception ex) {
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            return;
        }
        try {
            doRecord(request, response, ex);
        } catch (Exception e) {
            log.error("记录操作日志失败，不影响本次请求本身（响应已经处理完）", e);
        }
    }

    private void doRecord(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        long durationMs = startTime == null ? 0L : System.currentTimeMillis() - startTime;
        operationLogApplicationService.record(
                resolveOperatorId(),
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString(),
                resolveRequestBody(request),
                response.getStatus(),
                ex == null ? null : ex.getMessage(),
                resolveClientIp(request),
                durationMs);
    }

    /** 拿不到当前登录用户就记 null，不影响日志本身落库——理论上 /manage/** 已强制鉴权，正常都能拿到。 */
    private String resolveOperatorId() {
        try {
            return CurrentUserContext.get().userId();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private String resolveRequestBody(HttpServletRequest request) {
        if (!(request instanceof ContentCachingRequestWrapper wrapper)) {
            return null;
        }
        byte[] content = wrapper.getContentAsByteArray();
        if (content.length == 0) {
            return null;
        }
        String body = new String(content, StandardCharsets.UTF_8);
        return body.length() > MAX_BODY_LENGTH ? body.substring(0, MAX_BODY_LENGTH) : body;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
