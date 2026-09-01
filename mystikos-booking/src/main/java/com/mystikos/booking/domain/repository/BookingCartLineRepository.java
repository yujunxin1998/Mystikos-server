package com.mystikos.booking.domain.repository;

import com.mystikos.booking.domain.model.BookingCartLine;

import java.util.List;

public interface BookingCartLineRepository {

    BookingCartLine save(BookingCartLine line);

    List<BookingCartLine> findAllByPatron(Long patronId);

    /** 按 id 取选中的行，只返回属于该用户的（不属于的静默过滤掉，调用方按数量不匹配判断是否有非法/他人的 id）。 */
    List<BookingCartLine> findByIdsAndPatron(Long patronId, List<Long> ids);

    void deleteByPatronAndIds(Long patronId, List<Long> ids);
}
