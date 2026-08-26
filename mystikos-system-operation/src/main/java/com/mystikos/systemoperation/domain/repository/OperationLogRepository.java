package com.mystikos.systemoperation.domain.repository;

import com.mystikos.common.result.PageResult;
import com.mystikos.systemoperation.domain.model.OperationLog;

import java.time.OffsetDateTime;

public interface OperationLogRepository {

    void save(OperationLog operationLog);

    PageResult<OperationLog> findPage(String operatorId, String pathKeyword, OffsetDateTime occurredFrom,
                                       OffsetDateTime occurredTo, int pageNum, int pageSize);
}
