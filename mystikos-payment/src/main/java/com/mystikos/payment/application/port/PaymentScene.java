package com.mystikos.payment.application.port;

/**
 * 下单场景，决定网关内部调用哪个具体产品接口。支付宝/微信同一个 provider 下
 * 三种场景返回的下单结果形状完全不同（跳转链接/二维码/App 调起参数），
 * 见 {@link PaymentPayloadType}。Stripe 不区分场景，一律按 {@link #DEFAULT} 处理。
 */
public enum PaymentScene {
    /** Stripe 用；走 Payment Element/Stripe.js，不需要额外区分场景。 */
    DEFAULT,
    /** PC 网页扫码：支付宝当面付预下单 / 微信 Native 下单，返回二维码内容。 */
    PC_QR,
    /** 手机浏览器内支付：支付宝 wap 支付 / 微信 H5 下单，返回跳转链接。 */
    WAP_H5,
    /** 原生 App 内调起：支付宝 App 支付 / 微信 App 下单，返回 App SDK 调起参数。 */
    APP
}
