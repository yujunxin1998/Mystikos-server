package com.mystikos.booking.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mystikos.booking.domain.model.BookingCartLine;
import com.mystikos.booking.domain.model.TimeRange;
import com.mystikos.booking.domain.repository.BookingCartLineRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class BookingCartLineRepositoryImpl implements BookingCartLineRepository {

    private final BookingCartLineMapper mapper;

    public BookingCartLineRepositoryImpl(BookingCartLineMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public BookingCartLine save(BookingCartLine line) {
        BookingCartLinePO po = new BookingCartLinePO();
        po.setId(line.getId());
        po.setPatronId(line.getPatronId());
        po.setCompanionId(line.getCompanionId());
        po.setTimeRangeStart(line.getTimeRange().start());
        po.setTimeRangeEnd(line.getTimeRange().end());
        po.setDurationHours(line.getDurationHours());
        if (po.getId() == null) {
            po.setCreatedAt(OffsetDateTime.now());
            mapper.insert(po);
            line.assignId(po.getId());
        } else {
            mapper.updateById(po);
        }
        return line;
    }

    @Override
    public List<BookingCartLine> findAllByPatron(Long patronId) {
        return mapper.selectList(Wrappers.<BookingCartLinePO>lambdaQuery()
                        .eq(BookingCartLinePO::getPatronId, patronId)
                        .orderByDesc(BookingCartLinePO::getCreatedAt))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<BookingCartLine> findByIdsAndPatron(Long patronId, List<Long> ids) {
        return mapper.selectList(Wrappers.<BookingCartLinePO>lambdaQuery()
                        .eq(BookingCartLinePO::getPatronId, patronId)
                        .in(BookingCartLinePO::getId, ids))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteByPatronAndIds(Long patronId, List<Long> ids) {
        mapper.delete(Wrappers.<BookingCartLinePO>lambdaQuery()
                .eq(BookingCartLinePO::getPatronId, patronId)
                .in(BookingCartLinePO::getId, ids));
    }

    private BookingCartLine toDomain(BookingCartLinePO po) {
        return BookingCartLine.restore(po.getId(), po.getPatronId(), po.getCompanionId(),
                new TimeRange(po.getTimeRangeStart(), po.getTimeRangeEnd()), po.getDurationHours());
    }
}
