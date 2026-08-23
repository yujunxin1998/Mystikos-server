package com.mystikos.identity.application.service;

import com.mystikos.identity.domain.model.AssessmentResult;
import com.mystikos.identity.domain.model.CompanionIdentityApplicationStatus;
import com.mystikos.identity.domain.model.Gender;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record CompanionIdentityApplicationView(
        Long id,
        Long userId,
        String applicantNickname,
        String applicantPhone,
        String applicantEmail,
        String applicantRegionCode,
        String realName,
        Gender gender,
        LocalDate birthDate,
        String selfIntro,
        String gameNickname,
        String gameRankProofUrl,
        List<TagView> tags,
        String contactCountryCode,
        String contactPhone,
        String contactEmail,
        CompanionIdentityApplicationStatus status,
        Long reviewerId,
        String reviewerNickname,
        AssessmentResult reviewResult,
        String reviewComment,
        OffsetDateTime reviewedAt,
        OffsetDateTime createdAt
) {
}
