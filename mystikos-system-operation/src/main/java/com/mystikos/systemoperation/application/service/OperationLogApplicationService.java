package com.mystikos.systemoperation.application.service;

import com.mystikos.common.result.PageResult;
import com.mystikos.systemoperation.domain.model.OperationLog;
import com.mystikos.systemoperation.domain.repository.OperationLogRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 供 {@code OperationLogInterceptor} 调用落库，以及后台管理接口分页检索。
 * 记录失败只应影响调用方（拦截器那边已经 try/catch 兜底），这里不额外吞异常。
 */
@Service
public class OperationLogApplicationService {

    private final OperationLogRepository operationLogRepository;

    public OperationLogApplicationService(OperationLogRepository operationLogRepository) {
        this.operationLogRepository = operationLogRepository;
    }

    public void record(String operatorId, String httpMethod, String requestPath, String queryString,
                        String requestBody, Integer responseStatus, String errorMessage, String clientIp,
                        long durationMs) {
        operationLogRepository.save(OperationLog.record(operatorId, httpMethod, requestPath, queryString,
                requestBody, responseStatus, errorMessage, clientIp, durationMs));
    }

    public PageResult<OperationLogView> listPage(String operatorId, String pathKeyword, OffsetDateTime occurredFrom,
                                                  OffsetDateTime occurredTo, int pageNum, int pageSize) {
        PageResult<OperationLog> page = operationLogRepository.findPage(
                operatorId, pathKeyword, occurredFrom, occurredTo, pageNum, pageSize);
        List<OperationLogView> views = page.records().stream().map(OperationLogView::from).toList();
        return PageResult.of(views, page.total(), page.pageNum(), page.pageSize());
    }
}
