package com.mystikos.gifting.application.service;

import com.mystikos.common.event.DomainEventPublisher;
import com.mystikos.gifting.application.command.SendGiftCommand;
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

    private final GiftCatalogRepository giftCatalogRepository;
    private final GiftTransactionRepository giftTransactionRepository;
    private final DomainEventPublisher eventPublisher;

    public GiftApplicationService(GiftCatalogRepository giftCatalogRepository,
                                   GiftTransactionRepository giftTransactionRepository,
                                   DomainEventPublisher eventPublisher) {
        this.giftCatalogRepository = giftCatalogRepository;
        this.giftTransactionRepository = giftTransactionRepository;
        this.eventPublisher = eventPublisher;
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

        eventPublisher.publish(new GiftSentEvent(
                saved.getId(), saved.getPatronId(), saved.getCompanionId(),
                saved.getGiftId(), saved.getQuantity(), saved.getAmount()));
        return saved.getId();
    }
}
