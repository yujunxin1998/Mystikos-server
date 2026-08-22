/**
 * 支付账本（Payment &amp; Ledger）限界上下文：PaymentIntent、回调、退款、不可变账本。
 * 被 Booking / Commerce / Gifting 共用，通过 sourceType + sourceId 回指业务订单，
 * 不持有业务细节。领域模型见 docs/architecture/domain-model.md；内部五层结构
 * 实现时参照 mystikos-booking 模块，见 docs/architecture/module-structure.md。
 */
package com.mystikos.payment;
