package com.mystikos.identity.infrastructure.acl;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mystikos.identity.application.port.SmsSender;
import com.mystikos.identity.domain.model.VerificationPurpose;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

/** 阿里云国内短信验证码实现；只有显式启用时才会发送真实、计费的短信。 */
@Component
@ConditionalOnProperty(prefix = "mystikos.sms.aliyun", name = "enabled", havingValue = "true")
public class AliyunSmsSender implements SmsSender {

    private final Client client;
    private final ObjectMapper objectMapper;
    private final String signName;
    private final String verificationTemplateCode;

    public AliyunSmsSender(
            ObjectMapper objectMapper,
            @Value("${mystikos.sms.aliyun.access-key-id}") String accessKeyId,
            @Value("${mystikos.sms.aliyun.access-key-secret}") String accessKeySecret,
            @Value("${mystikos.sms.aliyun.endpoint:dysmsapi.aliyuncs.com}") String endpoint,
            @Value("${mystikos.sms.aliyun.sign-name}") String signName,
            @Value("${mystikos.sms.aliyun.verification-template-code}") String verificationTemplateCode
    ) throws Exception {
        this.objectMapper = objectMapper;
        this.signName = signName;
        this.verificationTemplateCode = verificationTemplateCode;
        this.client = new Client(new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret)
                .setEndpoint(endpoint));
    }

    @Override
    public void sendVerificationCode(String phoneNumber, String code, VerificationPurpose purpose) {
        try {
            SendSmsRequest request = new SendSmsRequest()
                    .setPhoneNumbers(phoneNumber)
                    .setSignName(signName)
                    .setTemplateCode(verificationTemplateCode)
                    .setTemplateParam(objectMapper.writeValueAsString(Map.of("code", code)));
            SendSmsResponse response = client.sendSms(request);
            if (response.getBody() == null || !"OK".equals(response.getBody().getCode())) {
                String message = response.getBody() == null ? "empty response" : response.getBody().getMessage();
                throw new IllegalStateException("Aliyun SMS send failed: " + message);
            }
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize SMS template parameters", e);
        } catch (Exception e) {
            throw new IllegalStateException("Aliyun SMS request failed", e);
        }
    }
}
