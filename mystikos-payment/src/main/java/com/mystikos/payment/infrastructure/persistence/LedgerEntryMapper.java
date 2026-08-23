package com.mystikos.payment.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LedgerEntryMapper extends BaseMapper<LedgerEntryPO> {
}
