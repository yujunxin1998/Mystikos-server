package com.mystikos.booking.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookingCartLineMapper extends BaseMapper<BookingCartLinePO> {
}
