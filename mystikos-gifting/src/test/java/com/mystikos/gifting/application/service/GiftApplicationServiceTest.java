package com.mystikos.gifting.application.service;

import com.mystikos.common.event.DomainEventPublisher;
import com.mystikos.gifting.application.command.SendGiftCommand;
import com.mystikos.gifting.application.port.PaymentPort;
import com.mystikos.gifting.domain.GiftingException;
import com.mystikos.gifting.domain.event.GiftRefundedEvent;
import com.mystikos.gifting.domain.event.GiftSentEvent;
import com.mystikos.gifting.domain.model.GiftCatalogItem;
import com.mystikos.gifting.domain.model.GiftTier;
import com.mystikos.gifting.domain.model.GiftTransaction;
import com.mystikos.gifting.domain.model.GiftTransactionStatus;
import com.mystikos.gifting.domain.model.UnlockRule;
import com.mystikos.gifting.domain.repository.GiftCatalogRepository;
import com.mystikos.gifting.domain.repository.GiftTierRepository;
import com.mystikos.gifting.domain.repository.GiftTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 覆盖秘典的核心公式：intimacyValue = amount x 档位倍率，且只影响亲密度，不影响
 * 钱包扣款金额；以及退款只能发生一次。
 */
class GiftApplicationServiceTest {

    private GiftCatalogRepository giftCatalogRepository;
    private GiftTierRepository giftTierRepository;
    private GiftTransactionRepository giftTransactionRepository;
    private DomainEventPublisher eventPublisher;
    private PaymentPort paymentPort;
    private GiftApplicationService service;

    @BeforeEach
    void setUp() {
        giftCatalogRepository = mock(GiftCatalogRepository.class);
        giftTierRepository = mock(GiftTierRepository.class);
        giftTransactionRepository = mock(GiftTransactionRepository.class);
        eventPublisher = mock(DomainEventPublisher.class);
        paymentPort = mock(PaymentPort.class);
        service = new GiftApplicationService(giftCatalogRepository, giftTierRepository,
                giftTransactionRepository, eventPublisher, paymentPort);

        GiftCatalogItem item = new GiftCatalogItem(10L, "AMETHYST_SCEPTER", "紫水晶权杖", "icon",
                BigDecimal.valueOf(188), 2L, UnlockRule.none(), true);
        GiftTier tier = new GiftTier(2L, "RARE", "稀有秘藏", "Rare Arcana", BigDecimal.valueOf(1.5), 2, true);
        when(giftCatalogRepository.findById(10L)).thenReturn(Optional.of(item));
        when(giftTierRepository.findById(2L)).thenReturn(Optional.of(tier));
        when(giftTransactionRepository.sumQuantityByPatronAndGift(any(), any())).thenReturn(0L);
        when(giftTransactionRepository.sumAmountByPatron(any())).thenReturn(BigDecimal.ZERO);

        AtomicLong idSequence = new AtomicLong(1);
        when(giftTransactionRepository.save(any())).thenAnswer(invocation -> {
            GiftTransaction transaction = invocation.getArgument(0, GiftTransaction.class);
            if (transaction.getId() == null) {
                transaction.assignId(idSequence.getAndIncrement());
            }
            return transaction;
        });
    }

    @Test
    void sendGiftAppliesTierMultiplierOnlyToIntimacyValue() {
        Long transactionId = service.sendGift(new SendGiftCommand(1001L, 2002L, 10L, 2));

        assertThat(transactionId).isNotNull();
        // amount = 188 x 2 = 376（原价，不含倍率），钱包按这个金额扣款
        verify(paymentPort).debitWallet(eq(1001L), eq(2002L), eq(transactionId), eq(BigDecimal.valueOf(376)), eq("CNY"));

        org.mockito.ArgumentCaptor<GiftSentEvent> captor = org.mockito.ArgumentCaptor.forClass(GiftSentEvent.class);
        verify(eventPublisher).publish(captor.capture());
        GiftSentEvent event = captor.getValue();
        assertThat(event.getAmount()).isEqualByComparingTo("376");
        // intimacyValue = 376 x 1.5 = 564
        assertThat(event.getIntimacyValue()).isEqualByComparingTo("564");
    }

    @Test
    void refundTwiceIsRejected() {
        GiftTransaction refunded = GiftTransaction.restore(99L, 1001L, 2002L, 10L, 1,
                BigDecimal.valueOf(188), BigDecimal.valueOf(1.5), BigDecimal.valueOf(282),
                OffsetDateTime.now(), GiftTransactionStatus.REFUNDED);
        when(giftTransactionRepository.findById(99L)).thenReturn(Optional.of(refunded));

        assertThatThrownBy(() -> service.refundGiftTransaction(99L))
                .isInstanceOf(GiftingException.class);
        verify(paymentPort, never()).refundWallet(any(), any(), any(), any(), any());
    }

    @Test
    void refundCompletedTransactionReversesWalletAndPublishesEvent() {
        GiftTransaction completed = GiftTransaction.restore(100L, 1001L, 2002L, 10L, 1,
                BigDecimal.valueOf(188), BigDecimal.valueOf(1.5), BigDecimal.valueOf(282),
                OffsetDateTime.now(), GiftTransactionStatus.COMPLETED);
        when(giftTransactionRepository.findById(100L)).thenReturn(Optional.of(completed));

        service.refundGiftTransaction(100L);

        verify(paymentPort).refundWallet(eq(1001L), eq(2002L), eq(100L), eq(BigDecimal.valueOf(188)), eq("CNY"));
        org.mockito.ArgumentCaptor<GiftRefundedEvent> captor = org.mockito.ArgumentCaptor.forClass(GiftRefundedEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().getIntimacyValue()).isEqualByComparingTo("282");
    }
}
