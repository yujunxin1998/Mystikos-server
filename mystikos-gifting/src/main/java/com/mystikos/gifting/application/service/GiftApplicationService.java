package com.mystikos.gifting.application.service;

import com.mystikos.common.event.DomainEventPublisher;
import com.mystikos.gifting.application.command.SaveGiftCommand;
import com.mystikos.gifting.application.command.SaveGiftTierCommand;
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
import com.mystikos.gifting.domain.model.UnlockRuleType;
import com.mystikos.gifting.domain.repository.GiftCatalogRepository;
import com.mystikos.gifting.domain.repository.GiftTierRepository;
import com.mystikos.gifting.domain.repository.GiftTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class GiftApplicationService {

    /**
     * 结算币种：星辉石 1:1 兑换人民币，赠礼场景要支持支付宝/微信钱包充值，这两家网关
     * 只认 CNY——固定 EUR 会导致这两个网关在建单阶段直接拒绝，见架构文档"现状与真实差距"。
     */
    private static final String DEFAULT_CURRENCY = "CNY";

    private final GiftCatalogRepository giftCatalogRepository;
    private final GiftTierRepository giftTierRepository;
    private final GiftTransactionRepository giftTransactionRepository;
    private final DomainEventPublisher eventPublisher;
    private final PaymentPort paymentPort;

    public GiftApplicationService(GiftCatalogRepository giftCatalogRepository,
                                   GiftTierRepository giftTierRepository,
                                   GiftTransactionRepository giftTransactionRepository,
                                   DomainEventPublisher eventPublisher,
                                   PaymentPort paymentPort) {
        this.giftCatalogRepository = giftCatalogRepository;
        this.giftTierRepository = giftTierRepository;
        this.giftTransactionRepository = giftTransactionRepository;
        this.eventPublisher = eventPublisher;
        this.paymentPort = paymentPort;
    }

    public List<GiftCatalogItem> listCatalog() {
        return giftCatalogRepository.findAllActive();
    }

    public List<GiftTier> listTiers() {
        return giftTierRepository.findAllActive();
    }

    @Transactional
    public Long sendGift(SendGiftCommand command) {
        GiftCatalogItem item = giftCatalogRepository.findById(command.giftId())
                .filter(GiftCatalogItem::isActive)
                .orElseThrow(() -> GiftingException.notFound(command.giftId()));
        GiftTier tier = giftTierRepository.findById(item.getTierId())
                .orElseThrow(() -> GiftingException.tierNotFound(item.getTierId()));

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
        BigDecimal intimacyValue = amount.multiply(tier.getMultiplier());
        GiftTransaction transaction = GiftTransaction.send(command.patronId(), command.companionId(),
                command.giftId(), command.quantity(), amount, tier.getMultiplier(), intimacyValue);
        GiftTransaction saved = giftTransactionRepository.save(transaction);

        // 余额不足这里会抛异常，@Transactional 让上面刚 save 的 GiftTransaction 一起回滚，
        // 不会出现"记了赠礼但没扣到钱"的账目。
        paymentPort.debitWallet(saved.getPatronId(), saved.getCompanionId(), saved.getId(), amount, DEFAULT_CURRENCY);

        eventPublisher.publish(new GiftSentEvent(saved.getId(), saved.getPatronId(), saved.getCompanionId(),
                saved.getGiftId(), saved.getQuantity(), saved.getAmount(), saved.getIntimacyValue()));
        return saved.getId();
    }

    /**
     * 管理端退款：反向钱包转账（退老板、扣陪玩）+ 标记流水 REFUNDED，成功后发
     * GiftRefundedEvent 供 Relationship 扣减亲密度累计值；VIP 累计消费的扣减走
     * Payment 自己发布的 PaymentRefundedEvent，不经过这个事件。
     */
    @Transactional
    public void refundGiftTransaction(Long transactionId) {
        GiftTransaction transaction = giftTransactionRepository.findById(transactionId)
                .orElseThrow(() -> GiftingException.transactionNotFound(transactionId));
        if (transaction.getStatus() != GiftTransactionStatus.COMPLETED) {
            throw GiftingException.alreadyRefunded(transactionId);
        }
        transaction.refund();
        giftTransactionRepository.save(transaction);

        paymentPort.refundWallet(transaction.getPatronId(), transaction.getCompanionId(),
                transaction.getId(), transaction.getAmount(), DEFAULT_CURRENCY);

        eventPublisher.publish(new GiftRefundedEvent(transaction.getId(), transaction.getPatronId(),
                transaction.getCompanionId(), transaction.getAmount(), transaction.getIntimacyValue()));
    }

    // ---- 管理端：礼物目录 / 档位 CRUD，运营配置数据，不用改代码即可扩充 ----

    public List<GiftCatalogItem> listAllGiftsForAdmin() {
        return giftCatalogRepository.findAll();
    }

    public List<GiftTier> listAllTiersForAdmin() {
        return giftTierRepository.findAll();
    }

    @Transactional
    public Long saveGift(SaveGiftCommand command) {
        giftTierRepository.findById(command.tierId())
                .orElseThrow(() -> GiftingException.tierNotFound(command.tierId()));
        UnlockRule rule = command.unlockRuleType() == null || command.unlockRuleType() == UnlockRuleType.NONE
                ? UnlockRule.none()
                : new UnlockRule(command.unlockRuleType(), command.unlockRuleThreshold());
        GiftCatalogItem item = new GiftCatalogItem(command.id(), command.code(), command.name(), command.icon(),
                command.price(), command.tierId(), rule, command.active());
        return giftCatalogRepository.save(item).getId();
    }

    @Transactional
    public Long saveGiftTier(SaveGiftTierCommand command) {
        GiftTier tier = new GiftTier(command.id(), command.code(), command.displayName(), command.displayNameEn(),
                command.multiplier(), command.sortOrder(), command.active());
        return giftTierRepository.save(tier).getId();
    }
}
