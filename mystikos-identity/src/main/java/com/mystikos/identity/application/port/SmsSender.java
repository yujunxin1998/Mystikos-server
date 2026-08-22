package com.mystikos.identity.application.port;

import com.mystikos.identity.domain.model.VerificationPurpose;

/**
 * 出站端口：发短信。短信没有 SMTP 那样的通用协议，必须接具体厂商（Twilio/阿里云/腾讯云……）才有真实实现，
 * 厂商还没定，当前只有 {@code UnconfiguredSmsSender}（打日志占位，见 infrastructure/acl），
 * 选定厂商后新增一个真实实现替换掉，用例代码不用改。
 */
public interface SmsSender {

    void sendVerificationCode(String phoneNumber, String code, VerificationPurpose purpose);
}
