package com.mystikos.identity.infrastructure.acl;

import com.mystikos.identity.application.port.EmailSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 本地开发/未配置 SMTP 时的兜底：邮件只打日志，不真实发送。
 * {@code mystikos.mail.enabled} 为 true 时会被 {@link SmtpEmailSender} 顶掉。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "mystikos.mail", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LoggingEmailSender implements EmailSender {

    @Override
    public void send(String to, String subject, String body) {
        log.warn("[邮件服务未启用，仅打印不真实发送] to={}, subject={}, body={}", to, subject, body);
    }
}
