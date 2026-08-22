package com.mystikos.identity.infrastructure.acl;

import com.mystikos.identity.application.port.SmsSender;
import com.mystikos.identity.domain.model.VerificationPurpose;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 短信服务商还没选定（面向欧洲/未来美洲澳洲市场，候选是 Twilio 这类有全球号码覆盖的厂商，
 * 不是国内短信的阿里云/腾讯云——待有账号后再评估），先打日志占位，不真实发送。
 * 选定厂商后新增一个真实实现替换掉这个 Bean，{@link com.mystikos.identity.application.port.SmsSender}
 * 接口和调用方（ChannelRoutingVerificationCodeSender）都不用改。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "mystikos.sms.aliyun", name = "enabled", havingValue = "false", matchIfMissing = true)
public class UnconfiguredSmsSender implements SmsSender {

    @Override
    public void sendVerificationCode(String phoneNumber, String code, VerificationPurpose purpose) {
        log.warn("[短信服务未启用，仅打印不真实发送] to={}, code={}, purpose={}", phoneNumber, code, purpose);
    }
}
