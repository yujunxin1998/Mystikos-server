package com.mystikos.commerce.domain.repository;

import com.mystikos.commerce.domain.model.PatronAddress;

import java.util.List;
import java.util.Optional;

public interface PatronAddressRepository {

    Optional<PatronAddress> findById(Long id);

    List<PatronAddress> findAllByPatron(Long patronId);

    PatronAddress save(PatronAddress address);

    void deleteById(Long id, Long patronId);

    /** 把该用户名下所有地址的默认标记清掉，供"设为默认"操作先清后设。 */
    void clearDefault(Long patronId);
}
