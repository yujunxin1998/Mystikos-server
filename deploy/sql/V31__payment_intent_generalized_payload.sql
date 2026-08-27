-- 接入支付宝/微信支付：PaymentIntent 的下单结果不再只是 Stripe 的 client_secret 一种形状，
-- 支付宝/微信按场景还有跳转链接/二维码/App 调起参数，统一改成 payload_type + payload（JSON 文本），
-- 见 com.mystikos.payment.application.port.GatewayIntentResult。

ALTER TABLE payment_intent DROP COLUMN client_secret;
ALTER TABLE payment_intent ADD COLUMN payload_type VARCHAR(32);
ALTER TABLE payment_intent ADD COLUMN payload TEXT;

COMMENT ON COLUMN payment_intent.payload_type IS 'CLIENT_SECRET/REDIRECT_URL/QR_CODE/APP_INVOKE_PARAMS';
COMMENT ON COLUMN payment_intent.payload IS '序列化后的 Map<String,String>，key 随 payload_type 而定';
COMMENT ON COLUMN payment_intent.gateway_provider IS '网关标识，如 stripe/alipay/wechat_pay；内部钱包扣款记 INTERNAL_WALLET，没有真正调用外部网关';
