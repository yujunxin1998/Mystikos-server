package com.mystikos.identity.application.port;

/**
 * 出站端口：发邮件。真实实现走通用 SMTP（不绑定具体厂商，见 infrastructure/acl 的
 * SmtpEmailSender），本地没配置 SMTP 时退化成打日志（LoggingEmailSender）。
 */
public interface EmailSender {

    void send(String to, String subject, String body);
}
