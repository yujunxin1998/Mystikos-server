package com.mystikos.identity.infrastructure.acl;

import com.mystikos.identity.application.port.EmailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * 通用 SMTP 发信，不绑定具体厂商——阿里云邮件推送/SendGrid/Mailgun/自建邮件服务器的 SMTP 中继
 * 都能接，只要填对 {@code spring.mail.*} 的 host/port/账号密码。只在
 * {@code mystikos.mail.enabled=true} 时生效，默认关闭（本地没配 SMTP 账号时用
 * {@link LoggingEmailSender} 兜底，不会尝试连一个不存在的邮件服务器报错）。
 */
@Component
@ConditionalOnProperty(prefix = "mystikos.mail", name = "enabled", havingValue = "true")
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;
    private final String from;

    public SmtpEmailSender(JavaMailSender mailSender, @Value("${mystikos.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
