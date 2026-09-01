package com.mystikos.gifting.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GiftTransactionTest {

    @Test
    void sendRejectsNonPositiveQuantity() {
        assertThatThrownBy(() -> GiftTransaction.send(1L, 2L, 3L, 0,
                BigDecimal.TEN, BigDecimal.ONE, BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refundTransitionsCompletedToRefunded() {
        GiftTransaction transaction = GiftTransaction.send(1L, 2L, 3L, 1,
                BigDecimal.TEN, BigDecimal.valueOf(1.5), BigDecimal.valueOf(15));

        transaction.refund();

        assertThat(transaction.getStatus()).isEqualTo(GiftTransactionStatus.REFUNDED);
    }

    @Test
    void refundingTwiceThrows() {
        GiftTransaction transaction = GiftTransaction.send(1L, 2L, 3L, 1,
                BigDecimal.TEN, BigDecimal.ONE, BigDecimal.TEN);
        transaction.refund();

        assertThatThrownBy(transaction::refund).isInstanceOf(IllegalStateException.class);
    }
}
