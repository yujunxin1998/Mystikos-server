package com.mystikos.identity.infrastructure.crypto;

import com.mystikos.identity.application.command.CredentialType;
import com.mystikos.identity.domain.IdentityException;
import com.mystikos.identity.domain.IdentityResponseCode;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LoginCredentialResolverTest {

    @Test
    void verificationCodeLoginPassesCredentialThroughUnchangedRegardlessOfEncryptionSwitch() {
        LoginEncryptionProperties properties = new LoginEncryptionProperties();
        properties.setEnabled(true);
        RsaLoginCredentialDecryptor decryptor = Mockito.mock(RsaLoginCredentialDecryptor.class);
        LoginCredentialResolver resolver = new LoginCredentialResolver(properties, decryptor);

        String result = resolver.resolve(CredentialType.VERIFICATION_CODE, "123456", null, null);

        assertThat(result).isEqualTo("123456");
        verifyNoInteractions(decryptor);
    }

    @Test
    void verificationCodeLoginRejectsBlankCode() {
        LoginEncryptionProperties properties = new LoginEncryptionProperties();
        RsaLoginCredentialDecryptor decryptor = Mockito.mock(RsaLoginCredentialDecryptor.class);
        LoginCredentialResolver resolver = new LoginCredentialResolver(properties, decryptor);

        assertThatThrownBy(() -> resolver.resolve(CredentialType.VERIFICATION_CODE, "  ", null, null))
                .isInstanceOfSatisfying(IdentityException.class,
                        e -> assertThat(e.getResultCode()).isEqualTo(IdentityResponseCode.CREDENTIAL_INVALID));
    }

    @Test
    void passwordLoginFallsBackToPlainCredentialWhenEncryptionDisabled() {
        LoginEncryptionProperties properties = new LoginEncryptionProperties();
        properties.setEnabled(false);
        RsaLoginCredentialDecryptor decryptor = Mockito.mock(RsaLoginCredentialDecryptor.class);
        LoginCredentialResolver resolver = new LoginCredentialResolver(properties, decryptor);

        String result = resolver.resolve(CredentialType.PASSWORD, "plain-password", null, null);

        assertThat(result).isEqualTo("plain-password");
        verifyNoInteractions(decryptor);
    }

    @Test
    void passwordLoginRejectsBlankPlainCredentialWhenEncryptionDisabled() {
        LoginEncryptionProperties properties = new LoginEncryptionProperties();
        properties.setEnabled(false);
        RsaLoginCredentialDecryptor decryptor = Mockito.mock(RsaLoginCredentialDecryptor.class);
        LoginCredentialResolver resolver = new LoginCredentialResolver(properties, decryptor);

        assertThatThrownBy(() -> resolver.resolve(CredentialType.PASSWORD, null, null, null))
                .isInstanceOfSatisfying(IdentityException.class,
                        e -> assertThat(e.getResultCode()).isEqualTo(IdentityResponseCode.CREDENTIAL_INVALID));
    }

    @Test
    void passwordLoginRejectsPlainCredentialFallbackWhenEncryptionEnabled() {
        LoginEncryptionProperties properties = new LoginEncryptionProperties();
        properties.setEnabled(true);
        RsaLoginCredentialDecryptor decryptor = Mockito.mock(RsaLoginCredentialDecryptor.class);
        LoginCredentialResolver resolver = new LoginCredentialResolver(properties, decryptor);

        // 只传明文 credential，不传 keyId/encryptedCredential —— 必须被拒绝，不能悄悄回退明文。
        assertThatThrownBy(() -> resolver.resolve(CredentialType.PASSWORD, "plain-password", null, null))
                .isInstanceOfSatisfying(IdentityException.class, e ->
                        assertThat(e.getResultCode()).isEqualTo(IdentityResponseCode.LOGIN_ENCRYPTION_REQUIRED));
        verifyNoInteractions(decryptor);
    }

    @Test
    void passwordLoginDelegatesToDecryptorWhenEncryptionEnabled() {
        LoginEncryptionProperties properties = new LoginEncryptionProperties();
        properties.setEnabled(true);
        RsaLoginCredentialDecryptor decryptor = Mockito.mock(RsaLoginCredentialDecryptor.class);
        when(decryptor.decrypt(eq("login-key-v1"), eq("cipher-base64"))).thenReturn("decrypted-password");
        LoginCredentialResolver resolver = new LoginCredentialResolver(properties, decryptor);

        String result = resolver.resolve(CredentialType.PASSWORD, null, "login-key-v1", "cipher-base64");

        assertThat(result).isEqualTo("decrypted-password");
    }
}
