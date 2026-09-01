package com.mystikos.membership.application.service;

import java.math.BigDecimal;

public record MembershipView(Long patronId, int tierLevel, String tierCode, String tierDisplayName,
                              BigDecimal cumulativeSpend) {
}
