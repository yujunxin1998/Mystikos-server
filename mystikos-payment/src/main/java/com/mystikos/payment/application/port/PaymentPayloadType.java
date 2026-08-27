package com.mystikos.payment.application.port;

/**
 * {@link GatewayIntentResult#payload()} 里到底装的是什么，前端按这个字段判断怎么消费
 * payload——调 Stripe.js（CLIENT_SECRET）、跳转（REDIRECT_URL）、渲染二维码（QR_CODE），
 * 还是把参数原样传给 App 内支付宝/微信 SDK（APP_INVOKE_PARAMS）。
 */
public enum PaymentPayloadType {
    CLIENT_SECRET,
    REDIRECT_URL,
    QR_CODE,
    APP_INVOKE_PARAMS
}
