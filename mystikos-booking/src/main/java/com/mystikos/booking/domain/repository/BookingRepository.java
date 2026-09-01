package com.mystikos.booking.domain.repository;

import com.mystikos.booking.domain.model.BookingOrder;
import com.mystikos.common.result.PageResult;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 领域层只定义接口，不依赖 MyBatis-Plus；实现在
 * infrastructure/persistence 包（BookingRepositoryImpl）。
 */
public interface BookingRepository {

    BookingOrder save(BookingOrder bookingOrder);

    Optional<BookingOrder> findById(Long id);

    PageResult<BookingOrder> findByPatronId(Long patronId, int pageNum, int pageSize);

    /** 支付有效期已过的 DRAFT/PENDING_PAYMENT 独立订单（不含归属预约组的子订单），供定时任务批量失效。 */
    List<BookingOrder> findExpirable(OffsetDateTime cutoff);

    /** 某个预约组下的所有子预约，供组级联操作（请求支付/标记已付/失效/取消）用。 */
    List<BookingOrder> findByGroupId(Long groupId);
}
