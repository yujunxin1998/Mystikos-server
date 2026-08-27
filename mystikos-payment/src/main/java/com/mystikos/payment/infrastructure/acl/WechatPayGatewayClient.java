package com.mystikos.payment.infrastructure.acl;

import com.mystikos.payment.application.port.GatewayEventType;
import com.mystikos.payment.application.port.GatewayIntentResult;
import com.mystikos.payment.application.port.GatewayWebhookEvent;
import com.mystikos.payment.application.port.PaymentGatewayClient;
import com.mystikos.payment.application.port.PaymentScene;
import com.mystikos.payment.application.port.WebhookNotification;
import com.mystikos.payment.domain.PaymentException;
import com.mystikos.payment.domain.model.PaymentProvider;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.cipher.SignatureResult;
import com.wechat.pay.java.core.exception.WechatPayException;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.app.AppService;
import com.wechat.pay.java.service.payments.h5.H5Service;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 微信支付 APIv3 直连（境内独立商户号）。只在配置了 mch-id 时注册这个 Bean，理由同
 * {@link AlipayGatewayClient}。用官方 {@code wechatpay-java} SDK，证书自动下载轮换、
 * 验签、AES-GCM 解密都不用自己维护。
 *
 * <p>PC 扫码走 Native 下单（返回二维码内容 code_url）、手机浏览器走 H5 下单（返回跳转链接
 * h5_url）、App 内调起走 App 下单——但 SDK 只负责拿到 prepay_id，App 调起所需的
 * appid/partnerid/prepayid/package/noncestr/timestamp/sign 六个字段要商户自己现签，
 * 见 {@link #buildAppInvokeParams}。
 *
 * <p>微信 out_trade_no 长度上限 32 位，我们内部的 idempotencyKey 是带横杠的 UUID（36 位），
 * 去掉横杠后正好 32 位十六进制字符，作为 out_trade_no/gatewayRef——webhook 回来的
 * out_trade_no 会原样匹配这个值。
 */
@Component
@ConditionalOnExpression("!'${mystikos.payment.wechat.mch-id:}'.isEmpty()")
public class WechatPayGatewayClient implements PaymentGatewayClient {

    private static final Logger log = LoggerFactory.getLogger(WechatPayGatewayClient.class);

    private final RSAAutoCertificateConfig config;
    private final NativePayService nativePayService;
    private final H5Service h5Service;
    private final AppService appService;
    private final RefundService refundService;
    private final NotificationParser notificationParser;
    private final String appId;
    private final String mchId;
    private final String notifyUrl;

    public WechatPayGatewayClient(@Value("${mystikos.payment.wechat.mch-id}") String mchId,
                                   @Value("${mystikos.payment.wechat.app-id}") String appId,
                                   @Value("${mystikos.payment.wechat.merchant-serial-no}") String merchantSerialNumber,
                                   @Value("${mystikos.payment.wechat.merchant-private-key-path}") String privateKeyPath,
                                   @Value("${mystikos.payment.wechat.api-v3-key}") String apiV3Key,
                                   @Value("${mystikos.payment.wechat.notify-url}") String notifyUrl) {
        this.config = new RSAAutoCertificateConfig.Builder()
                .merchantId(mchId)
                .privateKeyFromPath(privateKeyPath)
                .merchantSerialNumber(merchantSerialNumber)
                .apiV3Key(apiV3Key)
                .build();
        this.nativePayService = new NativePayService.Builder().config(config).build();
        this.h5Service = new H5Service.Builder().config(config).build();
        this.appService = new AppService.Builder().config(config).build();
        this.refundService = new RefundService.Builder().config(config).build();
        this.notificationParser = new NotificationParser(config);
        this.appId = appId;
        this.mchId = mchId;
        this.notifyUrl = notifyUrl;
    }

    @Override
    public String providerCode() {
        return PaymentProvider.WECHAT_PAY.code();
    }

    @Override
    public GatewayIntentResult createIntent(String idempotencyKey, BigDecimal amount, String currency,
                                             Map<String, String> metadata, PaymentScene scene) {
        String outTradeNo = idempotencyKey.replace("-", "");
        int totalFen = WechatAmountConverter.toFen(amount, currency);
        String description = "Mystikos-" + metadata.getOrDefault("sourceType", "ORDER") + "-" + metadata.get("sourceId");

        return switch (scene) {
            case PC_QR -> nativePrepay(outTradeNo, totalFen, description);
            case WAP_H5 -> h5Prepay(outTradeNo, totalFen, description);
            case APP -> appPrepay(outTradeNo, totalFen, description);
            case DEFAULT -> throw PaymentException.gatewayError("微信支付下单必须指定场景（PC_QR/WAP_H5/APP）");
        };
    }

    private GatewayIntentResult nativePrepay(String outTradeNo, int totalFen, String description) {
        var request = new com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest();
        request.setAppid(appId);
        request.setMchid(mchId);
        request.setDescription(description);
        request.setOutTradeNo(outTradeNo);
        request.setNotifyUrl(notifyUrl);
        var amount = new com.wechat.pay.java.service.payments.nativepay.model.Amount();
        amount.setTotal(totalFen);
        request.setAmount(amount);
        try {
            var response = nativePayService.prepay(request);
            return GatewayIntentResult.qrCode(outTradeNo, response.getCodeUrl());
        } catch (WechatPayException e) {
            log.warn("微信 Native 下单失败：{}", e.getMessage());
            throw PaymentException.gatewayError(e.getMessage());
        }
    }

    private GatewayIntentResult h5Prepay(String outTradeNo, int totalFen, String description) {
        var request = new com.wechat.pay.java.service.payments.h5.model.PrepayRequest();
        request.setAppid(appId);
        request.setMchid(mchId);
        request.setDescription(description);
        request.setOutTradeNo(outTradeNo);
        request.setNotifyUrl(notifyUrl);
        var amount = new com.wechat.pay.java.service.payments.h5.model.Amount();
        amount.setTotal(totalFen);
        request.setAmount(amount);
        var sceneInfo = new com.wechat.pay.java.service.payments.h5.model.SceneInfo();
        // 微信要求这里填真实用户端 IP，MVP 阶段拿不到就先填一个占位值；后续要接的话从请求上下文取。
        sceneInfo.setPayerClientIp("127.0.0.1");
        var h5Info = new com.wechat.pay.java.service.payments.h5.model.H5Info();
        h5Info.setType("Wap");
        sceneInfo.setH5Info(h5Info);
        request.setSceneInfo(sceneInfo);
        try {
            var response = h5Service.prepay(request);
            return GatewayIntentResult.redirectUrl(outTradeNo, response.getH5Url());
        } catch (WechatPayException e) {
            log.warn("微信 H5 下单失败：{}", e.getMessage());
            throw PaymentException.gatewayError(e.getMessage());
        }
    }

    private GatewayIntentResult appPrepay(String outTradeNo, int totalFen, String description) {
        var request = new com.wechat.pay.java.service.payments.app.model.PrepayRequest();
        request.setAppid(appId);
        request.setMchid(mchId);
        request.setDescription(description);
        request.setOutTradeNo(outTradeNo);
        request.setNotifyUrl(notifyUrl);
        var amount = new com.wechat.pay.java.service.payments.app.model.Amount();
        amount.setTotal(totalFen);
        request.setAmount(amount);
        try {
            var response = appService.prepay(request);
            return GatewayIntentResult.appInvokeParams(outTradeNo, buildAppInvokeParams(response.getPrepayId()));
        } catch (WechatPayException e) {
            log.warn("微信 App 下单失败：{}", e.getMessage());
            throw PaymentException.gatewayError(e.getMessage());
        }
    }

    /**
     * App 调起签名串固定格式：appid\ntimestamp\nnoncestr\nprepay_id=xxx\n，算法跟 APIv3
     * 请求签名一样，直接复用 {@code config.createSigner()}，不用自己单独实现 RSA 签名。
     */
    private Map<String, String> buildAppInvokeParams(String prepayId) {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String nonceStr = UUID.randomUUID().toString().replace("-", "");
        String message = appId + "\n" + timestamp + "\n" + nonceStr + "\n" + "prepay_id=" + prepayId + "\n";
        SignatureResult signatureResult = config.createSigner().sign(message);

        Map<String, String> params = new HashMap<>();
        params.put("appid", appId);
        params.put("partnerid", mchId);
        params.put("prepayid", prepayId);
        params.put("package", "Sign=WXPay");
        params.put("noncestr", nonceStr);
        params.put("timestamp", timestamp);
        params.put("sign", signatureResult.getSign());
        return params;
    }

    @Override
    public GatewayWebhookEvent parseWebhookEvent(WebhookNotification notification) {
        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(notification.headers().get("Wechatpay-Serial"))
                .nonce(notification.headers().get("Wechatpay-Nonce"))
                .signature(notification.headers().get("Wechatpay-Signature"))
                .timestamp(notification.headers().get("Wechatpay-Timestamp"))
                .body(notification.rawBody())
                .build();

        Transaction transaction;
        try {
            transaction = notificationParser.parse(requestParam, Transaction.class);
        } catch (WechatPayException e) {
            log.warn("微信支付 webhook 验签/解密失败：{}", e.getMessage());
            throw PaymentException.webhookSignatureInvalid();
        }

        String outTradeNo = transaction.getOutTradeNo();
        return switch (transaction.getTradeState()) {
            case SUCCESS -> new GatewayWebhookEvent(GatewayEventType.CAPTURED, outTradeNo, null);
            case CLOSED, PAYERROR, REVOKED -> new GatewayWebhookEvent(
                    GatewayEventType.FAILED, outTradeNo, transaction.getTradeStateDesc());
            default -> new GatewayWebhookEvent(GatewayEventType.IGNORED, null, null);
        };
    }

    @Override
    public void refund(String gatewayRef, BigDecimal amount) {
        int totalFen = WechatAmountConverter.toFen(amount, "CNY");
        CreateRequest request = new CreateRequest();
        request.setOutTradeNo(gatewayRef);
        request.setOutRefundNo("refund-" + gatewayRef);
        AmountReq amountReq = new AmountReq();
        // 这次退款没有部分退款场景（PaymentApplicationService.refund 总是退全款），
        // total（原订单金额）和 refund（本次退款金额）填同一个值。
        amountReq.setTotal((long) totalFen);
        amountReq.setRefund((long) totalFen);
        amountReq.setCurrency("CNY");
        request.setAmount(amountReq);
        try {
            refundService.create(request);
        } catch (WechatPayException e) {
            log.warn("微信支付退款失败：{}", e.getMessage());
            throw PaymentException.gatewayError(e.getMessage());
        }
    }
}
