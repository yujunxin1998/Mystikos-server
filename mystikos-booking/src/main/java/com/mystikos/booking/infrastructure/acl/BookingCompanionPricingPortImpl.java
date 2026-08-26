package com.mystikos.booking.infrastructure.acl;

import com.mystikos.booking.application.port.CompanionPricingPort;
import com.mystikos.booking.application.port.CompanionPricingSnapshot;
import com.mystikos.identity.domain.model.CompanionIdentityStatus;
import com.mystikos.identity.domain.model.CompanionProfile;
import com.mystikos.identity.domain.repository.CompanionProfileRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 防腐层：直接查 mystikos-identity 的 CompanionProfile 拿时薪，不新建独立的 Provider
 * Catalog 模块——代码库里陪玩定价只有这一份数据，重复建一份会产生两个真相来源。
 * 类名带 Booking 前缀，理由见 {@link BookingPaymentPortImpl} 类注释。
 */
@Component
public class BookingCompanionPricingPortImpl implements CompanionPricingPort {

    private final CompanionProfileRepository companionProfileRepository;

    public BookingCompanionPricingPortImpl(CompanionProfileRepository companionProfileRepository) {
        this.companionProfileRepository = companionProfileRepository;
    }

    @Override
    public CompanionPricingSnapshot getPricing(Long companionId) {
        return companionProfileRepository.findByUserId(companionId)
                .map(this::toSnapshot)
                .orElseGet(() -> new CompanionPricingSnapshot(BigDecimal.ZERO, false));
    }

    private CompanionPricingSnapshot toSnapshot(CompanionProfile profile) {
        boolean bookable = profile.getIdentityStatus() == CompanionIdentityStatus.ACTIVE;
        return new CompanionPricingSnapshot(profile.getHourlyRate(), bookable);
    }
}
