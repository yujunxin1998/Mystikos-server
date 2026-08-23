package com.mystikos.gifting.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 赠礼流水聚合根。不可变——赠礼一旦发生就是既成事实，没有"编辑一笔赠礼"这种操作，
 * 需要撤销的话建模上应该是新建一笔退款/补偿记录，不是改这笔流水（本期不做退款）。
 */
public class GiftTransaction {

    private Long id;
    private final Long patronId;
    private final Long companionId;
    private final Long giftId;
    private final int quantity;
    private final BigDecimal amount;
    private final OffsetDateTime sentAt;

    private GiftTransaction(Long id, Long patronId, Long companionId, Long giftId,
                             int quantity, BigDecimal amount, OffsetDateTime sentAt) {
        this.id = id;
        this.patronId = patronId;
        this.companionId = companionId;
        this.giftId = giftId;
        this.quantity = quantity;
        this.amount = amount;
        this.sentAt = sentAt;
    }

    public static GiftTransaction send(Long patronId, Long companionId, Long giftId,
                                        int quantity, BigDecimal amount) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("赠送数量必须大于 0");
        }
        return new GiftTransaction(null, patronId, companionId, giftId, quantity, amount, OffsetDateTime.now());
    }

    /** 从持久化数据重建聚合，仅供仓储实现调用。 */
    public static GiftTransaction restore(Long id, Long patronId, Long companionId, Long giftId,
                                           int quantity, BigDecimal amount, OffsetDateTime sentAt) {
        return new GiftTransaction(id, patronId, companionId, giftId, quantity, amount, sentAt);
    }

    /** 仅供仓储实现在插入后回填生成的主键。 */
    public void assignId(Long id) {
        this.id = id;
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

    public OffsetDateTime getSentAt() {
        return sentAt;
    }
}
