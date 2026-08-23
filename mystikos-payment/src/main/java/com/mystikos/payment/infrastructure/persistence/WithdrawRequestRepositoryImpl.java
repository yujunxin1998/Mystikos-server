package com.mystikos.payment.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mystikos.payment.domain.model.WithdrawRequest;
import com.mystikos.payment.domain.model.WithdrawStatus;
import com.mystikos.payment.domain.repository.WithdrawRequestRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class WithdrawRequestRepositoryImpl implements WithdrawRequestRepository {

    private final WithdrawRequestMapper mapper;

    public WithdrawRequestRepositoryImpl(WithdrawRequestMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public WithdrawRequest save(WithdrawRequest request) {
        WithdrawRequestPO po = toPO(request);
        if (po.getId() == null) {
            mapper.insert(po);
            request.assignId(po.getId());
        } else {
            mapper.updateById(po);
        }
        return request;
    }

    @Override
    public Optional<WithdrawRequest> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public List<WithdrawRequest> findAllByCompanion(Long companionId) {
        return mapper.selectList(Wrappers.<WithdrawRequestPO>lambdaQuery()
                        .eq(WithdrawRequestPO::getCompanionId, companionId)
                        .orderByDesc(WithdrawRequestPO::getId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private WithdrawRequestPO toPO(WithdrawRequest request) {
        WithdrawRequestPO po = new WithdrawRequestPO();
        po.setId(request.getId());
        po.setCompanionId(request.getCompanionId());
        po.setAmount(request.getAmount());
        po.setCurrency(request.getCurrency());
        po.setStatus(request.getStatus().name());
        po.setStripeTransferRef(request.getStripeTransferRef());
        po.setDecidedBy(request.getDecidedBy());
        po.setDecidedAt(request.getDecidedAt());
        po.setRejectReason(request.getRejectReason());
        po.setRequestedAt(request.getRequestedAt());
        return po;
    }

    private WithdrawRequest toDomain(WithdrawRequestPO po) {
        return WithdrawRequest.restore(po.getId(), po.getCompanionId(), po.getAmount(), po.getCurrency(),
                WithdrawStatus.valueOf(po.getStatus()), po.getStripeTransferRef(), po.getDecidedBy(),
                po.getDecidedAt(), po.getRejectReason(), po.getRequestedAt());
    }
}
