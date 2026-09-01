package com.mystikos.payment.domain.repository;

import com.mystikos.payment.domain.model.PaymentIntent;
import com.mystikos.payment.domain.model.SourceType;

import java.util.Optional;

public interface PaymentIntentRepository {

    PaymentIntent save(PaymentIntent intent);

    Optional<PaymentIntent> findById(Long id);

    Optional<PaymentIntent> findByGatewayRef(String gatewayRef);

    /** 找这个业务来源当前未走到终态（CAPTURED/FAILED/REFUNDED）的支付意图，用于幂等复用。 */
    Optional<PaymentIntent> findActiveBySource(SourceType sourceType, Long sourceId);

    /** 找这个业务来源最新的一条支付意图，不限状态——退款场景要拿到已经 CAPTURED 的那条。 */
    Optional<PaymentIntent> findLatestBySource(SourceType sourceType, Long sourceId);
}
