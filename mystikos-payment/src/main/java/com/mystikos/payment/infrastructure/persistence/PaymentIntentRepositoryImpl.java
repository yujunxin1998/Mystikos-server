package com.mystikos.payment.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mystikos.payment.domain.model.PaymentIntent;
import com.mystikos.payment.domain.model.PaymentStatus;
import com.mystikos.payment.domain.model.SourceType;
import com.mystikos.payment.domain.repository.PaymentIntentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PaymentIntentRepositoryImpl implements PaymentIntentRepository {

    private static final List<String> TERMINAL_STATUSES = List.of(
            PaymentStatus.CAPTURED.name(), PaymentStatus.FAILED.name(), PaymentStatus.REFUNDED.name());

    private final PaymentIntentMapper mapper;

    public PaymentIntentRepositoryImpl(PaymentIntentMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public PaymentIntent save(PaymentIntent intent) {
        PaymentIntentPO po = toPO(intent);
        if (po.getId() == null) {
            mapper.insert(po);
            intent.assignId(po.getId());
        } else {
            mapper.updateById(po);
        }
        return intent;
    }

    @Override
    public Optional<PaymentIntent> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public Optional<PaymentIntent> findByGatewayRef(String gatewayRef) {
        return Optional.ofNullable(mapper.selectOne(Wrappers.<PaymentIntentPO>lambdaQuery()
                        .eq(PaymentIntentPO::getGatewayRef, gatewayRef)))
                .map(this::toDomain);
    }

    @Override
    public Optional<PaymentIntent> findActiveBySource(SourceType sourceType, Long sourceId) {
        return Optional.ofNullable(mapper.selectOne(Wrappers.<PaymentIntentPO>lambdaQuery()
                        .eq(PaymentIntentPO::getSourceType, sourceType.name())
                        .eq(PaymentIntentPO::getSourceId, sourceId)
                        .notIn(PaymentIntentPO::getStatus, TERMINAL_STATUSES)
                        .orderByDesc(PaymentIntentPO::getId)
                        .last("LIMIT 1")))
                .map(this::toDomain);
    }

    private PaymentIntentPO toPO(PaymentIntent intent) {
        PaymentIntentPO po = new PaymentIntentPO();
        po.setId(intent.getId());
        po.setSourceType(intent.getSourceType().name());
        po.setSourceId(intent.getSourceId());
        po.setPatronId(intent.getPatronId());
        po.setAmount(intent.getAmount());
        po.setCurrency(intent.getCurrency());
        po.setStatus(intent.getStatus().name());
        po.setGatewayProvider(intent.getGatewayProvider());
        po.setGatewayRef(intent.getGatewayRef());
        po.setClientSecret(intent.getClientSecret());
        po.setIdempotencyKey(intent.getIdempotencyKey());
        po.setFailureReason(intent.getFailureReason());
        po.setCreatedAt(intent.getCreatedAt());
        po.setUpdatedAt(intent.getUpdatedAt());
        return po;
    }

    private PaymentIntent toDomain(PaymentIntentPO po) {
        return PaymentIntent.restore(po.getId(), SourceType.valueOf(po.getSourceType()), po.getSourceId(),
                po.getPatronId(), po.getAmount(), po.getCurrency(), PaymentStatus.valueOf(po.getStatus()),
                po.getGatewayProvider(), po.getGatewayRef(), po.getClientSecret(), po.getIdempotencyKey(),
                po.getFailureReason(), po.getCreatedAt(), po.getUpdatedAt());
    }
}
