package com.mystikos.payment.application.service;

import com.mystikos.common.event.DomainEventPublisher;
import com.mystikos.payment.application.command.CreatePaymentIntentCommand;
import com.mystikos.payment.application.port.PayoutGatewayClient;
import com.mystikos.payment.domain.PaymentException;
import com.mystikos.payment.domain.event.PaymentCapturedEvent;
import com.mystikos.payment.domain.event.PaymentRefundedEvent;
import com.mystikos.payment.domain.model.PaymentIntent;
import com.mystikos.payment.domain.model.CompanionPayoutAccount;
import com.mystikos.payment.domain.model.LedgerDirection;
import com.mystikos.payment.domain.model.LedgerEntry;
import com.mystikos.payment.domain.model.PaymentIntent;
import com.mystikos.payment.domain.model.PaymentProvider;
import com.mystikos.payment.domain.model.SourceType;
import com.mystikos.payment.domain.model.Wallet;
import com.mystikos.payment.domain.model.WithdrawRequest;
import com.mystikos.payment.domain.repository.CompanionPayoutAccountRepository;
import com.mystikos.payment.domain.repository.LedgerEntryRepository;
import com.mystikos.payment.domain.repository.PaymentIntentRepository;
import com.mystikos.payment.application.port.PaymentScene;
import com.mystikos.payment.domain.repository.WalletRepository;
import com.mystikos.payment.domain.repository.WithdrawRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * 内部记账余额用例：钱包充值、礼物打赏走余额扣款、陪玩提现走 Stripe Connect 打款。
 * 见 docs/architecture/prd-alignment.md 第3节点名的 Wallet/WithdrawRequest 缺口。
 */
@Service
public class WalletApplicationService {

    private final WalletRepository walletRepository;
    private final PaymentIntentRepository paymentIntentRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final WithdrawRequestRepository withdrawRequestRepository;
    private final CompanionPayoutAccountRepository companionPayoutAccountRepository;
    private final PayoutGatewayClient payoutGatewayClient;
    private final PaymentApplicationService paymentApplicationService;
    private final DomainEventPublisher eventPublisher;

    public WalletApplicationService(WalletRepository walletRepository,
                                     PaymentIntentRepository paymentIntentRepository,
                                     LedgerEntryRepository ledgerEntryRepository,
                                     WithdrawRequestRepository withdrawRequestRepository,
                                     CompanionPayoutAccountRepository companionPayoutAccountRepository,
                                     PayoutGatewayClient payoutGatewayClient,
                                     PaymentApplicationService paymentApplicationService,
                                     DomainEventPublisher eventPublisher) {
        this.walletRepository = walletRepository;
        this.paymentIntentRepository = paymentIntentRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.withdrawRequestRepository = withdrawRequestRepository;
        this.companionPayoutAccountRepository = companionPayoutAccountRepository;
        this.payoutGatewayClient = payoutGatewayClient;
        this.paymentApplicationService = paymentApplicationService;
        this.eventPublisher = eventPublisher;
    }

    public Wallet getBalance(Long userId, String currency) {
        return walletRepository.findOrCreate(userId, currency);
    }

    /** 用真实支付方式给自己的余额充值，sourceId 用 userId——充值没有自己的业务单据。 */
    public PaymentIntentResult requestRecharge(Long userId, BigDecimal amount, String currency,
                                                PaymentProvider provider, PaymentScene scene) {
        return paymentApplicationService.createIntent(
                new CreatePaymentIntentCommand(SourceType.WALLET_RECHARGE, userId, userId, amount, currency,
                        provider, scene));
    }

    /**
     * 充值到账后把钱记入余额。由 {@link com.mystikos.payment.infrastructure.acl.WalletRechargeCapturedListener}
     * 订阅 {@link PaymentCapturedEvent}（sourceType=WALLET_RECHARGE）后调用，不对外暴露给其他模块。
     */
    @Transactional
    public void onRechargeCaptured(Long userId, BigDecimal amount, String currency) {
        Wallet wallet = walletRepository.findOrCreate(userId, currency);
        requireSameCurrency(wallet, currency);
        walletRepository.credit(wallet.getId(), amount);
    }

    /**
     * 赠礼扣款：先扣老板余额，成功后全额转给陪玩余额（本轮不做平台抽成），
     * 全程记一条 CAPTURED 的 PaymentIntent + 两条 LedgerEntry，保证内部转账也留痕。
     * 扣款失败直接抛异常，调用方（Gifting）不会产生 GiftTransaction。
     */
    @Transactional
    public void debitForGift(Long patronId, Long companionId, Long giftTransactionId, BigDecimal amount, String currency) {
        Wallet patronWallet = walletRepository.findOrCreate(patronId, currency);
        requireSameCurrency(patronWallet, currency);
        boolean debited = walletRepository.debit(patronWallet.getId(), amount);
        if (!debited) {
            throw PaymentException.insufficientBalance();
        }
        Wallet companionWallet = walletRepository.findOrCreate(companionId, currency);
        requireSameCurrency(companionWallet, currency);
        walletRepository.credit(companionWallet.getId(), amount);

        PaymentIntent intent = PaymentIntent.createCapturedInternal(SourceType.GIFT, giftTransactionId, patronId,
                amount, currency, UUID.randomUUID().toString());
        PaymentIntent saved = paymentIntentRepository.save(intent);
        ledgerEntryRepository.save(LedgerEntry.record(saved.getId(), patronWallet.getId(),
                LedgerDirection.DEBIT, amount, currency));
        ledgerEntryRepository.save(LedgerEntry.record(saved.getId(), companionWallet.getId(),
                LedgerDirection.CREDIT, amount, currency));
        eventPublisher.publish(new PaymentCapturedEvent(saved.getId(), SourceType.GIFT, giftTransactionId,
                patronId, amount, currency));
    }

    /**
     * 赠礼退款：反向操作——从陪玩余额扣回，退回老板余额，把原来 CAPTURED 的内部 PaymentIntent
     * 转成 REFUNDED。陪玩余额不足（已提现/已花掉本轮不做部分退款处理，直接拒绝整笔退款）时
     * 抛异常，调用方（Gifting）让整个退款事务回滚，赠礼流水维持 COMPLETED。
     */
    @Transactional
    public void refundForGift(Long patronId, Long companionId, Long giftTransactionId, BigDecimal amount, String currency) {
        PaymentIntent intent = paymentIntentRepository.findLatestBySource(SourceType.GIFT, giftTransactionId)
                .orElseThrow(() -> PaymentException.notFound(giftTransactionId));

        Wallet companionWallet = walletRepository.findOrCreate(companionId, currency);
        requireSameCurrency(companionWallet, currency);
        boolean debited = walletRepository.debit(companionWallet.getId(), amount);
        if (!debited) {
            throw PaymentException.insufficientBalance();
        }
        Wallet patronWallet = walletRepository.findOrCreate(patronId, currency);
        requireSameCurrency(patronWallet, currency);
        walletRepository.credit(patronWallet.getId(), amount);

        intent.markRefunded();
        PaymentIntent saved = paymentIntentRepository.save(intent);
        ledgerEntryRepository.save(LedgerEntry.record(saved.getId(), companionWallet.getId(),
                LedgerDirection.DEBIT, amount, currency));
        ledgerEntryRepository.save(LedgerEntry.record(saved.getId(), patronWallet.getId(),
                LedgerDirection.CREDIT, amount, currency));
        eventPublisher.publish(new PaymentRefundedEvent(saved.getId(), SourceType.GIFT, giftTransactionId,
                patronId, amount, currency));
    }

    /** 提现申请即刻冻结（扣减）对应余额，防止审核期间被同一笔余额重复申请提现。 */
    @Transactional
    public WithdrawRequest requestWithdraw(Long companionId, BigDecimal amount, String currency) {
        Wallet wallet = walletRepository.findOrCreate(companionId, currency);
        requireSameCurrency(wallet, currency);
        boolean debited = walletRepository.debit(wallet.getId(), amount);
        if (!debited) {
            throw PaymentException.insufficientBalance();
        }
        ledgerEntryRepository.save(LedgerEntry.record(null, wallet.getId(), LedgerDirection.DEBIT, amount, currency));
        WithdrawRequest request = WithdrawRequest.create(companionId, amount, currency);
        return withdrawRequestRepository.save(request);
    }

    public List<WithdrawRequest> listWithdrawRequests(Long companionId) {
        return withdrawRequestRepository.findAllByCompanion(companionId);
    }

    /** 审批通过并立即打款；打款失败（网关报错）整个事务回滚，申请停留在 PENDING_REVIEW 可以重试审批。 */
    @Transactional
    public WithdrawRequest approveWithdraw(Long withdrawId, Long reviewerId) {
        WithdrawRequest request = withdrawRequestRepository.findById(withdrawId)
                .orElseThrow(() -> PaymentException.withdrawRequestNotFound(withdrawId));
        CompanionPayoutAccount account = companionPayoutAccountRepository.findByUserId(request.getCompanionId())
                .orElseThrow(PaymentException::payoutAccountNotReady);
        if (!payoutGatewayClient.isPayoutReady(account.getStripeConnectAccountId())) {
            throw PaymentException.payoutAccountNotReady();
        }

        request.approve(reviewerId);
        withdrawRequestRepository.save(request);

        String transferRef = payoutGatewayClient.transferToConnectAccount(account.getStripeConnectAccountId(),
                request.getAmount(), request.getCurrency(), UUID.randomUUID().toString());
        request.markPaid(transferRef);
        return withdrawRequestRepository.save(request);
    }

    /** 驳回时把冻结的余额退回给陪玩。 */
    @Transactional
    public WithdrawRequest rejectWithdraw(Long withdrawId, Long reviewerId, String reason) {
        WithdrawRequest request = withdrawRequestRepository.findById(withdrawId)
                .orElseThrow(() -> PaymentException.withdrawRequestNotFound(withdrawId));
        request.reject(reviewerId, reason);
        withdrawRequestRepository.save(request);

        Wallet wallet = walletRepository.findOrCreate(request.getCompanionId(), request.getCurrency());
        walletRepository.credit(wallet.getId(), request.getAmount());
        ledgerEntryRepository.save(LedgerEntry.record(null, wallet.getId(),
                LedgerDirection.CREDIT, request.getAmount(), request.getCurrency()));
        return request;
    }

    /** 发起 Stripe Connect Express 入驻；已有账户直接复用，返回新的 onboarding 链接（Stripe 链接有效期短）。 */
    @Transactional
    public String startConnectOnboarding(Long companionId, String email, String returnUrl, String refreshUrl) {
        CompanionPayoutAccount account = companionPayoutAccountRepository.findByUserId(companionId)
                .orElseGet(() -> {
                    String connectAccountId = payoutGatewayClient.createConnectAccount(email);
                    CompanionPayoutAccount created = CompanionPayoutAccount.create(companionId, connectAccountId);
                    return companionPayoutAccountRepository.save(created);
                });
        return payoutGatewayClient.createConnectOnboardingLink(account.getStripeConnectAccountId(), returnUrl, refreshUrl);
    }

    /**
     * 每个钱包目前假设单一结算币种（见计划里标注的后续工作：多币种换汇）。
     * 一旦钱包已经用某个币种开出来，后续操作传别的币种直接拒绝，而不是静默混记
     * ——余额字段是一个数字，混了两种货币的金额进去就是错的，而且没法事后纠正。
     */
    private void requireSameCurrency(Wallet wallet, String currency) {
        if (!wallet.getCurrency().equalsIgnoreCase(currency)) {
            throw PaymentException.gatewayError(
                    "钱包结算币种为 " + wallet.getCurrency() + "，与请求币种 " + currency + " 不一致");
        }
    }
}
