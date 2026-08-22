package com.mystikos.identity.application.port;

import com.mystikos.identity.domain.model.AuthChannel;
import com.mystikos.identity.domain.model.VerificationPurpose;

/**
 * 出站端口：把验证码发给用户。SMS/邮件服务商还没选型，
 * 本地开发用 {@code LoggingVerificationCodeSender} 打日志代替真实发送
 * （见 infrastructure/acl），选型后新增一个真实实现替换掉，用例代码不用改。
 */
public interface VerificationCodeSender {

    void send(AuthChannel channel, String identifier, String code, VerificationPurpose purpose);
}
