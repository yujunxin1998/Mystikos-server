package com.mystikos.payment.application.port;

import java.util.Map;

/**
 * 网关无关的下单结果。不同 {@link PaymentPayloadType} 对应不同的 payload key 约定：
 * CLIENT_SECRET→{@code clientSecret}，REDIRECT_URL→{@code redirectUrl}，
 * QR_CODE→{@code qrCode}，APP_INVOKE_PARAMS 由各网关自行约定 key（支付宝是
 * {@code orderString} 一个字段，微信 App 调起是 appid/partnerid/prepayid/package/
 * noncestr/timestamp/sign 六个字段）。用 Map 而不是拍平成固定字段，是为了以后加
 * 小程序 JSAPI 场景时不用再改这个类型。
 */
public record GatewayIntentResult(String gatewayRef, PaymentPayloadType payloadType, Map<String, String> payload) {

    public static GatewayIntentResult clientSecret(String gatewayRef, String clientSecret) {
        return new GatewayIntentResult(gatewayRef, PaymentPayloadType.CLIENT_SECRET, Map.of("clientSecret", clientSecret));
    }

    public static GatewayIntentResult redirectUrl(String gatewayRef, String url) {
        return new GatewayIntentResult(gatewayRef, PaymentPayloadType.REDIRECT_URL, Map.of("redirectUrl", url));
    }

    public static GatewayIntentResult qrCode(String gatewayRef, String qrCode) {
        return new GatewayIntentResult(gatewayRef, PaymentPayloadType.QR_CODE, Map.of("qrCode", qrCode));
    }

    public static GatewayIntentResult appInvokeParams(String gatewayRef, Map<String, String> params) {
        return new GatewayIntentResult(gatewayRef, PaymentPayloadType.APP_INVOKE_PARAMS, params);
    }
}
