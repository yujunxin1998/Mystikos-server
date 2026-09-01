package com.mystikos.payment.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mystikos.payment.application.port.PaymentPayloadType;
import com.mystikos.payment.domain.model.PaymentIntent;
import com.mystikos.payment.domain.model.PaymentStatus;
import com.mystikos.payment.domain.model.SourceType;
import com.mystikos.payment.domain.repository.PaymentIntentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class PaymentIntentRepositoryImpl implements PaymentIntentRepository {

    private static final List<String> TERMINAL_STATUSES = List.of(
            PaymentStatus.CAPTURED.name(), PaymentStatus.FAILED.name(), PaymentStatus.REFUNDED.name());
    private static final TypeReference<Map<String, String>> PAYLOAD_TYPE_REF = new TypeReference<>() {
    };

    private final PaymentIntentMapper mapper;
    private final ObjectMapper objectMapper;

    public PaymentIntentRepositoryImpl(PaymentIntentMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
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

    @Override
    public Optional<PaymentIntent> findLatestBySource(SourceType sourceType, Long sourceId) {
        return Optional.ofNullable(mapper.selectOne(Wrappers.<PaymentIntentPO>lambdaQuery()
                        .eq(PaymentIntentPO::getSourceType, sourceType.name())
                        .eq(PaymentIntentPO::getSourceId, sourceId)
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
        po.setPayloadType(intent.getPayloadType() == null ? null : intent.getPayloadType().name());
        po.setPayload(writePayload(intent.getPayload()));
        po.setIdempotencyKey(intent.getIdempotencyKey());
        po.setFailureReason(intent.getFailureReason());
        po.setCreatedAt(intent.getCreatedAt());
        po.setUpdatedAt(intent.getUpdatedAt());
        return po;
    }

    private PaymentIntent toDomain(PaymentIntentPO po) {
        PaymentPayloadType payloadType = po.getPayloadType() == null ? null : PaymentPayloadType.valueOf(po.getPayloadType());
        return PaymentIntent.restore(po.getId(), SourceType.valueOf(po.getSourceType()), po.getSourceId(),
                po.getPatronId(), po.getAmount(), po.getCurrency(), PaymentStatus.valueOf(po.getStatus()),
                po.getGatewayProvider(), po.getGatewayRef(), payloadType, readPayload(po.getPayload()),
                po.getIdempotencyKey(), po.getFailureReason(), po.getCreatedAt(), po.getUpdatedAt());
    }

    private String writePayload(Map<String, String> payload) {
        if (payload == null || payload.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("支付意图 payload 序列化失败", e);
        }
    }

    private Map<String, String> readPayload(String payload) {
        if (payload == null || payload.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(payload, PAYLOAD_TYPE_REF);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("支付意图 payload 反序列化失败", e);
        }
    }
}
