package com.mystikos.systemoperation.domain.model;

import java.time.OffsetDateTime;

/**
 * 一条后台操作留痕，只追加不修改。由 {@code OperationLogInterceptor} 对
 * {@code /api/v1/manage/**} 下的非 GET 请求自动生成，不需要各业务模块的
 * Controller 显式调用。
 */
public class OperationLog {

    private Long id;
    private final String operatorId;
    private final String httpMethod;
    private final String requestPath;
    private final String queryString;
    private final String requestBody;
    private final Integer responseStatus;
    private final boolean success;
    private final String errorMessage;
    private final String clientIp;
    private final long durationMs;
    private final OffsetDateTime occurredAt;

    private OperationLog(Long id, String operatorId, String httpMethod, String requestPath, String queryString,
                          String requestBody, Integer responseStatus, boolean success, String errorMessage,
                          String clientIp, long durationMs, OffsetDateTime occurredAt) {
        this.id = id;
        this.operatorId = operatorId;
        this.httpMethod = httpMethod;
        this.requestPath = requestPath;
        this.queryString = queryString;
        this.requestBody = requestBody;
        this.responseStatus = responseStatus;
        this.success = success;
        this.errorMessage = errorMessage;
        this.clientIp = clientIp;
        this.durationMs = durationMs;
        this.occurredAt = occurredAt;
    }

    public static OperationLog record(String operatorId, String httpMethod, String requestPath, String queryString,
                                       String requestBody, Integer responseStatus, String errorMessage,
                                       String clientIp, long durationMs) {
        boolean success = errorMessage == null && responseStatus != null && responseStatus < 400;
        return new OperationLog(null, operatorId, httpMethod, requestPath, queryString, requestBody,
                responseStatus, success, errorMessage, clientIp, durationMs, OffsetDateTime.now());
    }

    /** 从持久化数据重建，仅供仓储实现调用。 */
    public static OperationLog restore(Long id, String operatorId, String httpMethod, String requestPath,
                                        String queryString, String requestBody, Integer responseStatus,
                                        boolean success, String errorMessage, String clientIp, long durationMs,
                                        OffsetDateTime occurredAt) {
        return new OperationLog(id, operatorId, httpMethod, requestPath, queryString, requestBody, responseStatus,
                success, errorMessage, clientIp, durationMs, occurredAt);
    }

    public Long getId() {
        return id;
    }

    /** 仅供仓储实现在插入后回填生成的主键。 */
    public void assignId(Long id) {
        this.id = id;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public String getQueryString() {
        return queryString;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public Integer getResponseStatus() {
        return responseStatus;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getClientIp() {
        return clientIp;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }
}
