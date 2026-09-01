package com.mystikos.booking.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mystikos.booking.domain.model.BookingGroupStatus;
import com.mystikos.booking.domain.model.BookingOrderGroup;
import com.mystikos.booking.domain.repository.BookingOrderGroupRepository;
import com.mystikos.common.result.PageResult;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class BookingOrderGroupRepositoryImpl implements BookingOrderGroupRepository {

    private static final Set<BookingGroupStatus> EXPIRABLE_STATUSES =
            EnumSet.of(BookingGroupStatus.DRAFT, BookingGroupStatus.PENDING_PAYMENT);

    private final BookingOrderGroupMapper mapper;

    public BookingOrderGroupRepositoryImpl(BookingOrderGroupMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public BookingOrderGroup save(BookingOrderGroup group) {
        BookingOrderGroupPO po = toPO(group);
        if (po.getId() == null) {
            mapper.insert(po);
            group.assignId(po.getId());
        } else {
            mapper.updateById(po);
        }
        return group;
    }

    @Override
    public Optional<BookingOrderGroup> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public PageResult<BookingOrderGroup> findByPatronId(Long patronId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<BookingOrderGroupPO> rows = mapper.selectList(new LambdaQueryWrapper<BookingOrderGroupPO>()
                .eq(BookingOrderGroupPO::getPatronId, patronId)
                .orderByDesc(BookingOrderGroupPO::getCreatedAt));
        PageInfo<BookingOrderGroupPO> pageInfo = new PageInfo<>(rows);
        List<BookingOrderGroup> groups = rows.stream().map(this::toDomain).toList();
        return PageResult.of(groups, pageInfo.getTotal(), pageNum, pageSize);
    }

    @Override
    public List<BookingOrderGroup> findExpirableGroups(OffsetDateTime cutoff) {
        List<BookingOrderGroupPO> rows = mapper.selectList(new LambdaQueryWrapper<BookingOrderGroupPO>()
                .in(BookingOrderGroupPO::getStatus, EXPIRABLE_STATUSES.stream().map(Enum::name).toList())
                .lt(BookingOrderGroupPO::getCreatedAt, cutoff));
        return rows.stream().map(this::toDomain).toList();
    }

    private BookingOrderGroupPO toPO(BookingOrderGroup group) {
        BookingOrderGroupPO po = new BookingOrderGroupPO();
        po.setId(group.getId());
        po.setPatronId(group.getPatronId());
        po.setStatus(group.getStatus().name());
        po.setTotalAmount(group.getTotalAmount());
        po.setCreatedAt(group.getCreatedAt());
        po.setVersion(group.getVersion());
        return po;
    }

    private BookingOrderGroup toDomain(BookingOrderGroupPO po) {
        return BookingOrderGroup.restore(po.getId(), po.getPatronId(), BookingGroupStatus.valueOf(po.getStatus()),
                po.getTotalAmount(), po.getCreatedAt(), po.getVersion());
    }
}
