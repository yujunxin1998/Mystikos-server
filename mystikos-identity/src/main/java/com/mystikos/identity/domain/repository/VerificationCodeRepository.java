package com.mystikos.identity.domain.repository;

import com.mystikos.identity.domain.model.AuthChannel;
import com.mystikos.identity.domain.model.VerificationCode;
import com.mystikos.identity.domain.model.VerificationPurpose;

import java.util.Optional;

public interface VerificationCodeRepository {

    VerificationCode save(VerificationCode verificationCode);

    /** 同一 channel+identifier+purpose 可能发过好几次，只取最新一条判断是否有效。 */
    Optional<VerificationCode> findLatestActive(AuthChannel channel, String identifier, VerificationPurpose purpose);
}
