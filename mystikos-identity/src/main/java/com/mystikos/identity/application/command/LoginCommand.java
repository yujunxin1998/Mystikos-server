package com.mystikos.identity.application.command;

import com.mystikos.identity.domain.model.AuthChannel;

public record LoginCommand(
        AuthChannel channel,
        String identifier,
        CredentialType credentialType,
        String credential
) {

    /** 覆盖默认 toString，避免日志/审计不小心把明文密码或验证码打印出来。 */
    @Override
    public String toString() {
        return "LoginCommand[channel=" + channel + ", identifier=" + identifier
                + ", credentialType=" + credentialType + ", credential=***]";
    }
}
