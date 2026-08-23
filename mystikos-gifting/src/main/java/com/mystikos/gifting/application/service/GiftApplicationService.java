package com.mystikos.gifting.application.service;

import com.mystikos.common.event.DomainEventPublisher;
import com.mystikos.gifting.application.command.SendGiftCommand;
import com.mystikos.gifting.application.port.PaymentPort;
import com.mystikos.gifting.domain.GiftingException;
import com.mystikos.gifting.domain.event.GiftSentEvent;
import com.mystikos.gifting.domain.model.GiftCatalogItem;
import com.mystikos.gifting.domain.model.GiftTransaction;
import com.mystikos.gifting.domain.model.UnlockRule;
import com.mystikos.gifting.domain.repository.GiftCatalogRepository;
import com.mystikos.gifting.domain.repository.GiftTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class GiftApplicationService {

    /**
     * 结算币种暂时固定为欧元——GiftTransaction 聚合目前没有按赠礼存币种的字段，
     * 多币种支持留给后续。
     */
    private static final String DEFAULT_CURRENCY = "EUR";

    private final GiftCatalogRepository giftCatalogRepository;
    private final GiftTransactionRepository giftTransactionRepository;
    private final DomainEventPublisher eventPublisher;
    private final PaymentPort paymentPort;

    public GiftApplicationService(GiftCatalogRepository giftCatalogRepository,
                                   GiftTransactionRepository giftTransactionRepository,
                                   DomainEventPublisher eventPublisher,
                                   PaymentPort paymentPort) {
        this.giftCatalogRepository = giftCatalogRepository;
        this.giftTransactionRepository = giftTransactionRepository;
        this.eventPublisher = eventPublisher;
        this.paymentPort = paymentPort;
    }

    public List<GiftCatalogItem> listCatalog() {
        return giftCatalogRepository.findAllActive();
    }

    @Transactional
    public Long sendGift(SendGiftCommand command) {
        GiftCatalogItem item = giftCatalogRepository.findById(command.giftId())
                .filter(GiftCatalogItem::isActive)
                .orElseThrow(() -> GiftingException.notFound(command.giftId()));

        UnlockRule rule = item.getUnlockRule();
        if (!rule.isEvaluable()) {
            throw GiftingException.unlockRuleUnsupported();
        }
        long cumulativeCount = giftTransactionRepository.sumQuantityByPatronAndGift(
                command.patronId(), command.giftId());
        BigDecimal cumulativeSpend = giftTransactionRepository.sumAmountByPatron(command.patronId());
        if (!rule.isSatisfiedBy(cumulativeCount, cumulativeSpend)) {
            throw GiftingException.unlockRuleNotSatisfied();
        }

        BigDecimal amount = item.getPrice().multiply(BigDecimal.valueOf(command.quantity()));
        GiftTransaction transaction = GiftTransaction.send(
                command.patronId(), command.companionId(), command.giftId(), command.quantity(), amount);
        GiftTransaction saved = giftTransactionRepository.save(transaction);

        // 余额不足这里会抛异常，@Transactional 让上面刚 save 的 GiftTransaction 一起回滚，
        // 不会出现"记了赠礼但没扣到钱"的账目。
        paymentPort.debitWallet(saved.getPatronId(), saved.getCompanionId(), saved.getId(), amount, DEFAULT_CURRENCY);

        eventPublisher.publish(new GiftSentEvent(
                saved.getId(), saved.getPatronId(), saved.getCompanionId(),
                saved.getGiftId(), saved.getQuantity(), saved.getAmount()));
        return saved.getId();
    }
}
