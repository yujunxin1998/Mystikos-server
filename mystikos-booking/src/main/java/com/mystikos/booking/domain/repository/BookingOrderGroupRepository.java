package com.mystikos.booking.domain.repository;

import com.mystikos.booking.domain.model.BookingOrderGroup;
import com.mystikos.common.result.PageResult;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingOrderGroupRepository {

    BookingOrderGroup save(BookingOrderGroup group);

    Optional<BookingOrderGroup> findById(Long id);

    PageResult<BookingOrderGroup> findByPatronId(Long patronId, int pageNum, int pageSize);

    /** 支付有效期已过的 DRAFT/PENDING_PAYMENT 预约组，供定时任务批量失效。 */
    List<BookingOrderGroup> findExpirableGroups(OffsetDateTime cutoff);
}
