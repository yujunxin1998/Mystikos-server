package com.mystikos.systemoperation.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mystikos.common.result.PageResult;
import com.mystikos.systemoperation.domain.model.OperationLog;
import com.mystikos.systemoperation.domain.repository.OperationLogRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class OperationLogRepositoryImpl implements OperationLogRepository {

    private final OperationLogMapper mapper;

    public OperationLogRepositoryImpl(OperationLogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(OperationLog operationLog) {
        mapper.insert(toPO(operationLog));
    }

    @Override
    public PageResult<OperationLog> findPage(String operatorId, String pathKeyword, OffsetDateTime occurredFrom,
                                              OffsetDateTime occurredTo, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        LambdaQueryWrapper<OperationLogPO> query = new LambdaQueryWrapper<OperationLogPO>()
                .eq(StringUtils.hasText(operatorId), OperationLogPO::getOperatorId, operatorId)
                .like(StringUtils.hasText(pathKeyword), OperationLogPO::getRequestPath, pathKeyword)
                .ge(occurredFrom != null, OperationLogPO::getCreatedAt, occurredFrom)
                .le(occurredTo != null, OperationLogPO::getCreatedAt, occurredTo)
                .orderByDesc(OperationLogPO::getCreatedAt);
        List<OperationLogPO> rows = mapper.selectList(query);
        PageInfo<OperationLogPO> pageInfo = new PageInfo<>(rows);
        List<OperationLog> logs = rows.stream().map(this::toDomain).toList();
        return PageResult.of(logs, pageInfo.getTotal(), pageNum, pageSize);
    }

    private OperationLogPO toPO(OperationLog log) {
        OperationLogPO po = new OperationLogPO();
        po.setOperatorId(log.getOperatorId());
        po.setHttpMethod(log.getHttpMethod());
        po.setRequestPath(log.getRequestPath());
        po.setQueryString(log.getQueryString());
        po.setRequestBody(log.getRequestBody());
        po.setResponseStatus(log.getResponseStatus());
        po.setSuccess(log.isSuccess());
        po.setErrorMessage(log.getErrorMessage());
        po.setClientIp(log.getClientIp());
        po.setDurationMs(log.getDurationMs());
        po.setCreatedAt(log.getOccurredAt());
        return po;
    }

    private OperationLog toDomain(OperationLogPO po) {
        return OperationLog.restore(po.getId(), po.getOperatorId(), po.getHttpMethod(), po.getRequestPath(),
                po.getQueryString(), po.getRequestBody(), po.getResponseStatus(),
                Boolean.TRUE.equals(po.getSuccess()), po.getErrorMessage(), po.getClientIp(),
                po.getDurationMs() == null ? 0L : po.getDurationMs(), po.getCreatedAt());
    }
}
