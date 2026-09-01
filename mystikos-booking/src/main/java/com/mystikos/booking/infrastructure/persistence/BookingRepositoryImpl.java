package com.mystikos.booking.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mystikos.booking.domain.model.BookingOrder;
import com.mystikos.booking.domain.model.BookingStatus;
import com.mystikos.booking.domain.model.TimeRange;
import com.mystikos.booking.domain.repository.BookingRepository;
import com.mystikos.common.result.PageResult;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class BookingRepositoryImpl implements BookingRepository {

    private static final List<String> EXPIRABLE_STATUSES =
            List.of(BookingStatus.DRAFT.name(), BookingStatus.PENDING_PAYMENT.name());

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

    @Override
    public PageResult<BookingOrder> findByPatronId(Long patronId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<BookingOrderPO> rows = mapper.selectList(new LambdaQueryWrapper<BookingOrderPO>()
                .eq(BookingOrderPO::getPatronId, patronId)
                .orderByDesc(BookingOrderPO::getCreatedAt));
        PageInfo<BookingOrderPO> pageInfo = new PageInfo<>(rows);
        List<BookingOrder> orders = rows.stream().map(this::toDomain).toList();
        return PageResult.of(orders, pageInfo.getTotal(), pageNum, pageSize);
    }

    @Override
    public List<BookingOrder> findExpirable(OffsetDateTime cutoff) {
        // group_id IS NULL：归属预约组的子预约不能被这个普通扫描单独判过期，
        // 否则组本身的状态会永远停在 PENDING_PAYMENT——组内子预约的过期只能从
        // BookingOrderGroup 发起并级联，见 BookingApplicationService#expireOverdueGroups。
        List<BookingOrderPO> rows = mapper.selectList(new LambdaQueryWrapper<BookingOrderPO>()
                .in(BookingOrderPO::getStatus, EXPIRABLE_STATUSES)
                .lt(BookingOrderPO::getCreatedAt, cutoff)
                .isNull(BookingOrderPO::getGroupId));
        return rows.stream().map(this::toDomain).toList();
    }

    @Override
    public List<BookingOrder> findByGroupId(Long groupId) {
        List<BookingOrderPO> rows = mapper.selectList(new LambdaQueryWrapper<BookingOrderPO>()
                .eq(BookingOrderPO::getGroupId, groupId));
        return rows.stream().map(this::toDomain).toList();
    }

    private BookingOrderPO toPO(BookingOrder order) {
        BookingOrderPO po = new BookingOrderPO();
        po.setId(order.getId());
        po.setPatronId(order.getPatronId());
        po.setCompanionId(order.getCompanionId());
        po.setTimeRangeStart(order.getTimeRange().start());
        po.setTimeRangeEnd(order.getTimeRange().end());
        po.setDurationHours(order.getDurationHours());
        po.setPriceSnapshot(order.getPriceSnapshot());
        po.setStatus(order.getStatus().name());
        po.setCreatedAt(order.getCreatedAt());
        po.setVersion(order.getVersion());
        po.setGroupId(order.getGroupId());
        return po;
    }

    private BookingOrder toDomain(BookingOrderPO po) {
        return BookingOrder.restore(
                po.getId(),
                po.getPatronId(),
                po.getCompanionId(),
                new TimeRange(po.getTimeRangeStart(), po.getTimeRangeEnd()),
                po.getDurationHours(),
                po.getPriceSnapshot(),
                BookingStatus.valueOf(po.getStatus()),
                po.getCreatedAt(),
                po.getVersion(),
                po.getGroupId());
    }
}
