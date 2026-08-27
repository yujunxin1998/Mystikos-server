package com.mystikos.identity.infrastructure.crypto;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LoginEncryptionProperties.class)
public class LoginEncryptionConfig {
}
