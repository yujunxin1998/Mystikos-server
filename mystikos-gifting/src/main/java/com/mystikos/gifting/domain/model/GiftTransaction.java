package com.mystikos.gifting.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 赠礼流水聚合根。{@code amount} 是原价（= 单价 x 数量，不含档位倍率），驱动钱包扣款/
 * VIP 累计消费/排行榜；{@code intimacyValue}（= amount x 档位倍率，发送时快照）只驱动
 * 亲密度——两者故意分开，秘典的规则是"档位倍率只影响亲密度"，VIP/排行榜要看原价。
 * {@code tierMultiplierSnapshot} 单独存一份是为了退款/对账时不用反查档位当时的倍率，
 * 且目录后续调整倍率不会改写这笔历史流水的任何数字。
 */
public class GiftTransaction {

    private Long id;
    private final Long patronId;
    private final Long companionId;
    private final Long giftId;
    private final int quantity;
    private final BigDecimal amount;
    private final BigDecimal tierMultiplierSnapshot;
    private final BigDecimal intimacyValue;
    private final OffsetDateTime sentAt;
    private GiftTransactionStatus status;

    private GiftTransaction(Long id, Long patronId, Long companionId, Long giftId, int quantity,
                             BigDecimal amount, BigDecimal tierMultiplierSnapshot, BigDecimal intimacyValue,
                             OffsetDateTime sentAt, GiftTransactionStatus status) {
        this.id = id;
        this.patronId = patronId;
        this.companionId = companionId;
        this.giftId = giftId;
        this.quantity = quantity;
        this.amount = amount;
        this.tierMultiplierSnapshot = tierMultiplierSnapshot;
        this.intimacyValue = intimacyValue;
        this.sentAt = sentAt;
        this.status = status;
    }

    public static GiftTransaction send(Long patronId, Long companionId, Long giftId, int quantity,
                                        BigDecimal amount, BigDecimal tierMultiplierSnapshot, BigDecimal intimacyValue) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("赠送数量必须大于 0");
        }
        return new GiftTransaction(null, patronId, companionId, giftId, quantity, amount,
                tierMultiplierSnapshot, intimacyValue, OffsetDateTime.now(), GiftTransactionStatus.COMPLETED);
    }

    /** 从持久化数据重建聚合，仅供仓储实现调用。 */
    public static GiftTransaction restore(Long id, Long patronId, Long companionId, Long giftId, int quantity,
                                           BigDecimal amount, BigDecimal tierMultiplierSnapshot, BigDecimal intimacyValue,
                                           OffsetDateTime sentAt, GiftTransactionStatus status) {
        return new GiftTransaction(id, patronId, companionId, giftId, quantity, amount,
                tierMultiplierSnapshot, intimacyValue, sentAt, status);
    }

    /** 仅供仓储实现在插入后回填生成的主键。 */
    public void assignId(Long id) {
        this.id = id;
    }

    /**
     * 标记这笔赠礼已退款。调用方（GiftApplicationService）应在调用前自行校验状态并抛出
     * 带错误码的业务异常给用户看；这里的 IllegalStateException 是防御性的最后一道检查。
     */
    public void refund() {
        if (status != GiftTransactionStatus.COMPLETED) {
            throw new IllegalStateException("只有 COMPLETED 状态的赠礼可以退款，当前状态：" + status);
        }
        this.status = GiftTransactionStatus.REFUNDED;
    }

    public Long getId() {
        return id;
    }

    public Long getPatronId() {
        return patronId;
    }

    public Long getCompanionId() {
        return companionId;
    }

    public Long getGiftId() {
        return giftId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getTierMultiplierSnapshot() {
        return tierMultiplierSnapshot;
    }

    public BigDecimal getIntimacyValue() {
        return intimacyValue;
    }

    public OffsetDateTime getSentAt() {
        return sentAt;
    }

    public GiftTransactionStatus getStatus() {
        return status;
    }
}
