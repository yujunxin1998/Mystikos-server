package com.mystikos.payment.domain.model;

import com.mystikos.payment.application.port.PaymentPayloadType;
import com.mystikos.payment.domain.PaymentException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * 支付意图聚合根。被 Booking/Commerce/Gifting 各自的 PaymentPort 共用，通过 sourceType + sourceId
 * 回指业务订单，不持有业务细节。状态迁移全部收在聚合内部，照 BookingOrder 的
 * transition(expected, next) 收口模式，防止调用方绕过状态机。
 *
 * <p>gatewayProvider="INTERNAL_WALLET" 的记录代表钱包内部转账（礼物扣款），
 * 没有真正调用外部网关，创建时直接是 CAPTURED——保证"每一笔资金移动都有
 * PaymentIntent+LedgerEntry"这条不变量对内部转账也成立，不是只有外部支付才记账。
 *
 * <p>payloadType/payload 取代原来单一的 clientSecret 字段——Stripe 是 clientSecret，
 * 支付宝/微信按场景不同还有跳转链接/二维码/App 调起参数，形状不一样，
 * 见 {@link com.mystikos.payment.application.port.GatewayIntentResult}。
 */
public class PaymentIntent {

    private Long id;
    private final SourceType sourceType;
    private final Long sourceId;
    private final Long patronId;
    private final BigDecimal amount;
    private final String currency;
    private PaymentStatus status;
    private String gatewayProvider;
    private String gatewayRef;
    private PaymentPayloadType payloadType;
    private Map<String, String> payload;
    private final String idempotencyKey;
    private String failureReason;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private PaymentIntent(Long id, SourceType sourceType, Long sourceId, Long patronId,
                           BigDecimal amount, String currency, PaymentStatus status,
                           String gatewayProvider, String gatewayRef,
                           PaymentPayloadType payloadType, Map<String, String> payload,
                           String idempotencyKey, String failureReason,
                           OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.patronId = patronId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.gatewayProvider = gatewayProvider;
        this.gatewayRef = gatewayRef;
        this.payloadType = payloadType;
        this.payload = payload == null ? Map.of() : payload;
        this.idempotencyKey = idempotencyKey;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** 创建一笔需要走外部网关的支付意图，初始状态 CREATED。 */
    public static PaymentIntent createPending(SourceType sourceType, Long sourceId, Long patronId,
                                               BigDecimal amount, String currency, String idempotencyKey) {
        OffsetDateTime now = OffsetDateTime.now();
        return new PaymentIntent(null, sourceType, sourceId, patronId, amount, currency,
                PaymentStatus.CREATED, null, null, null, null, idempotencyKey, null, now, now);
    }

    /** 创建一笔内部钱包扣款记录，创建即完成（没有外部网关往返）。 */
    public static PaymentIntent createCapturedInternal(SourceType sourceType, Long sourceId, Long patronId,
                                                         BigDecimal amount, String currency, String idempotencyKey) {
        OffsetDateTime now = OffsetDateTime.now();
        return new PaymentIntent(null, sourceType, sourceId, patronId, amount, currency,
                PaymentStatus.CAPTURED, "INTERNAL_WALLET", idempotencyKey, null, null, idempotencyKey, null, now, now);
    }

    /** 从持久化数据重建聚合，仅供仓储实现调用。 */
    public static PaymentIntent restore(Long id, SourceType sourceType, Long sourceId, Long patronId,
                                         BigDecimal amount, String currency, PaymentStatus status,
                                         String gatewayProvider, String gatewayRef,
                                         PaymentPayloadType payloadType, Map<String, String> payload,
                                         String idempotencyKey, String failureReason,
                                         OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        return new PaymentIntent(id, sourceType, sourceId, patronId, amount, currency, status,
                gatewayProvider, gatewayRef, payloadType, payload, idempotencyKey, failureReason, createdAt, updatedAt);
    }

    /** 网关已建单，记下 gatewayRef/payload，转 REQUIRES_ACTION 等待用户在前端完成支付。 */
    public void markRequiresAction(String gatewayProvider, String gatewayRef,
                                    PaymentPayloadType payloadType, Map<String, String> payload) {
        transition(PaymentStatus.CREATED, PaymentStatus.REQUIRES_ACTION);
        this.gatewayProvider = gatewayProvider;
        this.gatewayRef = gatewayRef;
        this.payloadType = payloadType;
        this.payload = payload == null ? Map.of() : payload;
    }

    public void markCaptured() {
        transition(PaymentStatus.REQUIRES_ACTION, PaymentStatus.CAPTURED);
    }

    public void markFailed(String reason) {
        if (status != PaymentStatus.CREATED && status != PaymentStatus.REQUIRES_ACTION) {
            throw PaymentException.statusInvalid("期望状态 CREATED/REQUIRES_ACTION，实际状态 " + status);
        }
        status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markRefunded() {
        transition(PaymentStatus.CAPTURED, PaymentStatus.REFUNDED);
    }

    private void transition(PaymentStatus expected, PaymentStatus next) {
        if (status != expected) {
            throw PaymentException.statusInvalid("期望状态 " + expected + "，实际状态 " + status);
        }
        status = next;
        this.updatedAt = OffsetDateTime.now();
    }

    public boolean isTerminal() {
        return status == PaymentStatus.CAPTURED || status == PaymentStatus.FAILED || status == PaymentStatus.REFUNDED;
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public Long getPatronId() {
        return patronId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getGatewayProvider() {
        return gatewayProvider;
    }

    public String getGatewayRef() {
        return gatewayRef;
    }

    public PaymentPayloadType getPayloadType() {
        return payloadType;
    }

    public Map<String, String> getPayload() {
        return payload;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
