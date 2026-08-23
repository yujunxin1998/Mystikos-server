package com.mystikos.payment.domain.repository;

import com.mystikos.payment.domain.model.WithdrawRequest;

import java.util.List;
import java.util.Optional;

public interface WithdrawRequestRepository {

    WithdrawRequest save(WithdrawRequest request);

    Optional<WithdrawRequest> findById(Long id);

    List<WithdrawRequest> findAllByCompanion(Long companionId);
}
