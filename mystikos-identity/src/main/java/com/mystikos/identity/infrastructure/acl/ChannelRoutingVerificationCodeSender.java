package com.mystikos.identity.infrastructure.acl;

import com.mystikos.identity.application.port.EmailSender;
import com.mystikos.identity.application.port.SmsSender;
import com.mystikos.identity.application.port.VerificationCodeSender;
import com.mystikos.identity.domain.model.AuthChannel;
import com.mystikos.identity.domain.model.VerificationPurpose;
import org.springframework.stereotype.Component;

/**
 * 按 channel 把验证码路由到邮件或短信，真实发不发、发得出去发不出去是
 * {@link EmailSender}/{@link SmsSender} 各自实现的事，这里只管路由 + 文案。
 *
 * <p>文案用英文——目标市场先是欧洲，之后扩美洲/澳洲，不是国内用户为主；
 * 真要做多语言，得按用户的语言偏好挑模板，这里先不做，只留了这一个入口以后好扩。
 */
@Component
public class ChannelRoutingVerificationCodeSender implements VerificationCodeSender {

    private final EmailSender emailSender;
    private final SmsSender smsSender;

    public ChannelRoutingVerificationCodeSender(EmailSender emailSender, SmsSender smsSender) {
        this.emailSender = emailSender;
        this.smsSender = smsSender;
    }

    @Override
    public void send(AuthChannel channel, String identifier, String code, VerificationPurpose purpose) {
        String message = buildMessage(code, purpose);
        if (channel == AuthChannel.EMAIL) {
            emailSender.send(identifier, "Your Mystikos verification code", message);
        } else {
            smsSender.sendVerificationCode(identifier, code, purpose);
        }
    }

    private String buildMessage(String code, VerificationPurpose purpose) {
        String action = switch (purpose) {
            case REGISTER -> "complete your registration";
            case LOGIN -> "log in";
            case RESET_PASSWORD -> "reset your password";
        };
        return "Your Mystikos verification code is " + code + ". Use it to " + action
                + ". This code expires in 5 minutes and can only be used once.";
    }
}
