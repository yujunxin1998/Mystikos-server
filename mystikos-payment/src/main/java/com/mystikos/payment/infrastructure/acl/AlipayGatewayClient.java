package com.mystikos.payment.infrastructure.acl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.mystikos.payment.application.port.GatewayEventType;
import com.mystikos.payment.application.port.GatewayIntentResult;
import com.mystikos.payment.application.port.GatewayWebhookEvent;
import com.mystikos.payment.application.port.PaymentGatewayClient;
import com.mystikos.payment.application.port.PaymentScene;
import com.mystikos.payment.application.port.WebhookNotification;
import com.mystikos.payment.domain.PaymentException;
import com.mystikos.payment.domain.model.PaymentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付宝开放平台直连（境内独立商户号，公钥模式）。只在配置了 app-id 时注册这个 Bean，
 * 照 {@link StripeGatewayClient} 的写法——本地没配时 {@code PaymentGatewayRegistry} 找不到
 * "alipay" 这个 providerCode，调用方拿到"网关未配置"而不是启动失败。
 *
 * <p>PC 扫码走当面付预下单（返回二维码内容）、手机浏览器走 wap 支付（返回跳转链接）、
 * App 内调起走 App 支付（返回 orderString 原样交给客户端 SDK），
 * 分别对应 {@link PaymentScene#PC_QR}/{@link PaymentScene#WAP_H5}/{@link PaymentScene#APP}。
 */
@Component
@ConditionalOnExpression("!'${mystikos.payment.alipay.app-id:}'.isEmpty()")
public class AlipayGatewayClient implements PaymentGatewayClient {

    private static final Logger log = LoggerFactory.getLogger(AlipayGatewayClient.class);
    private static final String CHARSET = "UTF-8";
    private static final String SIGN_TYPE = "RSA2";

    private final AlipayClient alipayClient;
    private final String notifyUrl;
    private final String alipayPublicKey;

    public AlipayGatewayClient(@Value("${mystikos.payment.alipay.app-id}") String appId,
                               @Value("${mystikos.payment.alipay.merchant-private-key}") String merchantPrivateKey,
                               @Value("${mystikos.payment.alipay.alipay-public-key}") String alipayPublicKey,
                               @Value("${mystikos.payment.alipay.notify-url}") String notifyUrl,
                               @Value("${mystikos.payment.alipay.gateway-url:https://openapi.alipay.com/gateway.do}")
                               String gatewayUrl) {
        AlipayConfig config = new AlipayConfig();
        config.setServerUrl(gatewayUrl);
        config.setAppId(appId);
        config.setPrivateKey(merchantPrivateKey);
        config.setAlipayPublicKey(alipayPublicKey);
        config.setFormat("json");
        config.setCharset(CHARSET);
        config.setSignType(SIGN_TYPE);
        try {
            this.alipayClient = new DefaultAlipayClient(config);
        } catch (AlipayApiException e) {
            throw new IllegalStateException("支付宝客户端初始化失败，请检查 app-id/密钥配置", e);
        }
        this.notifyUrl = notifyUrl;
        this.alipayPublicKey = alipayPublicKey;
    }

    @Override
    public String providerCode() {
        return PaymentProvider.ALIPAY.code();
    }

    @Override
    public GatewayIntentResult createIntent(String idempotencyKey, BigDecimal amount, String currency,
                                             Map<String, String> metadata, PaymentScene scene) {
        String outTradeNo = idempotencyKey;
        String totalAmount = AlipayAmountConverter.toYuanString(amount, currency);
        String subject = "Mystikos-" + metadata.getOrDefault("sourceType", "ORDER") + "-" + metadata.get("sourceId");

        return switch (scene) {
            case PC_QR -> precreate(outTradeNo, totalAmount, subject);
            case WAP_H5 -> wapPay(outTradeNo, totalAmount, subject);
            case APP -> appPay(outTradeNo, totalAmount, subject);
            case DEFAULT -> throw PaymentException.gatewayError("支付宝下单必须指定场景（PC_QR/WAP_H5/APP）");
        };
    }

    private GatewayIntentResult precreate(String outTradeNo, String totalAmount, String subject) {
        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        model.setOutTradeNo(outTradeNo);
        model.setTotalAmount(totalAmount);
        model.setSubject(subject);
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl);
        try {
            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            if (!response.isSuccess()) {
                log.warn("支付宝当面付预下单失败：{}", response.getSubMsg());
                throw PaymentException.gatewayError(response.getSubMsg());
            }
            return GatewayIntentResult.qrCode(outTradeNo, response.getQrCode());
        } catch (AlipayApiException e) {
            log.warn("支付宝当面付预下单调用失败：{}", e.getErrMsg());
            throw PaymentException.gatewayError(e.getErrMsg());
        }
    }

    private GatewayIntentResult wapPay(String outTradeNo, String totalAmount, String subject) {
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(outTradeNo);
        model.setTotalAmount(totalAmount);
        model.setSubject(subject);
        model.setProductCode("QUICK_WAP_WAY");
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl);
        try {
            // 用 GET 方式拿一个可以直接跳转的 URL，不用 POST 方式那种要前端渲染自动提交表单的 HTML。
            AlipayTradeWapPayResponse response = alipayClient.pageExecute(request, "GET");
            return GatewayIntentResult.redirectUrl(outTradeNo, response.getBody());
        } catch (AlipayApiException e) {
            log.warn("支付宝 wap 支付下单失败：{}", e.getErrMsg());
            throw PaymentException.gatewayError(e.getErrMsg());
        }
    }

    private GatewayIntentResult appPay(String outTradeNo, String totalAmount, String subject) {
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setOutTradeNo(outTradeNo);
        model.setTotalAmount(totalAmount);
        model.setSubject(subject);
        model.setProductCode("QUICK_MSECURITY_PAY");
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl);
        try {
            AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);
            return GatewayIntentResult.appInvokeParams(outTradeNo, Map.of("orderString", response.getOrderStr()));
        } catch (AlipayApiException e) {
            log.warn("支付宝 App 支付下单失败：{}", e.getErrMsg());
            throw PaymentException.gatewayError(e.getErrMsg());
        }
    }

    /**
     * 支付宝异步通知的签名字段本身就在表单参数里，没有独立的签名头，见
     * {@link com.mystikos.payment.adapter.web.PaymentCallbackController#alipayWebhook}。
     */
    @Override
    public GatewayWebhookEvent parseWebhookEvent(WebhookNotification notification) {
        Map<String, String> params = notification.formParams();
        boolean valid;
        try {
            valid = AlipaySignature.rsaCheckV1(params, alipayPublicKey, CHARSET, SIGN_TYPE);
        } catch (AlipayApiException e) {
            log.warn("支付宝 webhook 验签异常：{}", e.getErrMsg());
            throw PaymentException.webhookSignatureInvalid();
        }
        if (!valid) {
            throw PaymentException.webhookSignatureInvalid();
        }

        String outTradeNo = params.get("out_trade_no");
        String tradeStatus = params.get("trade_status");
        return switch (tradeStatus == null ? "" : tradeStatus) {
            case "TRADE_SUCCESS", "TRADE_FINISHED" -> new GatewayWebhookEvent(GatewayEventType.CAPTURED, outTradeNo, null);
            case "TRADE_CLOSED" -> new GatewayWebhookEvent(GatewayEventType.FAILED, outTradeNo, "支付宝交易关闭");
            default -> new GatewayWebhookEvent(GatewayEventType.IGNORED, null, null);
        };
    }

    @Override
    public void refund(String gatewayRef, BigDecimal amount) {
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(gatewayRef);
        model.setRefundAmount(AlipayAmountConverter.toYuanString(amount, "CNY"));
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        request.setBizModel(model);
        try {
            AlipayTradeRefundResponse response = alipayClient.execute(request);
            if (!response.isSuccess()) {
                log.warn("支付宝退款失败：{}", response.getSubMsg());
                throw PaymentException.gatewayError(response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            log.warn("支付宝退款调用失败：{}", e.getErrMsg());
            throw PaymentException.gatewayError(e.getErrMsg());
        }
    }
}
