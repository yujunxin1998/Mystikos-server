package com.mystikos.systemoperation.application.service;

import com.mystikos.systemoperation.domain.model.OperationLog;

import java.time.OffsetDateTime;

public record OperationLogView(
        Long id,
        String operatorId,
        String httpMethod,
        String requestPath,
        String queryString,
        String requestBody,
        Integer responseStatus,
        boolean success,
        String errorMessage,
        String clientIp,
        long durationMs,
        OffsetDateTime occurredAt
) {

    public static OperationLogView from(OperationLog log) {
        return new OperationLogView(log.getId(), log.getOperatorId(), log.getHttpMethod(), log.getRequestPath(),
                log.getQueryString(), log.getRequestBody(), log.getResponseStatus(), log.isSuccess(),
                log.getErrorMessage(), log.getClientIp(), log.getDurationMs(), log.getOccurredAt());
    }
}
