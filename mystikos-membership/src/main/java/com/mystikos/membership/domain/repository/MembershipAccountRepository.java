package com.mystikos.membership.domain.repository;

import com.mystikos.membership.domain.model.MembershipAccount;

import java.util.Optional;

public interface MembershipAccountRepository {

    Optional<MembershipAccount> findByPatronId(Long patronId);

    MembershipAccount save(MembershipAccount account);
}
