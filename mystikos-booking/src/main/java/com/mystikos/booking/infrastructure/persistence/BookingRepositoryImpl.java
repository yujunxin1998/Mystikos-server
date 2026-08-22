package com.mystikos.booking.infrastructure.persistence;

import com.mystikos.booking.domain.model.BookingOrder;
import com.mystikos.booking.domain.model.BookingStatus;
import com.mystikos.booking.domain.model.TimeRange;
import com.mystikos.booking.domain.repository.BookingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class BookingRepositoryImpl implements BookingRepository {

    private final BookingOrderMapper mapper;

    public BookingRepositoryImpl(BookingOrderMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public BookingOrder save(BookingOrder bookingOrder) {
        BookingOrderPO po = toPO(bookingOrder);
        if (po.getId() == null) {
            mapper.insert(po);
            bookingOrder.assignId(po.getId());
        } else {
            mapper.updateById(po);
        }
        return bookingOrder;
    }

    @Override
    public Optional<BookingOrder> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    private BookingOrderPO toPO(BookingOrder order) {
        BookingOrderPO po = new BookingOrderPO();
        po.setId(order.getId());
        po.setPatronId(order.getPatronId());
        po.setCompanionId(order.getCompanionId());
        po.setSkuId(order.getSkuId());
        po.setTimeRangeStart(order.getTimeRange().start());
        po.setTimeRangeEnd(order.getTimeRange().end());
        po.setPriceSnapshot(order.getPriceSnapshot());
        po.setStatus(order.getStatus().name());
        po.setVersion(order.getVersion());
        return po;
    }

    private BookingOrder toDomain(BookingOrderPO po) {
        return BookingOrder.restore(
                po.getId(),
                po.getPatronId(),
                po.getCompanionId(),
                po.getSkuId(),
                new TimeRange(po.getTimeRangeStart(), po.getTimeRangeEnd()),
                po.getPriceSnapshot(),
                BookingStatus.valueOf(po.getStatus()),
                po.getVersion());
    }
}
