package com.mystikos.commerce.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InventoryStockMapper extends BaseMapper<InventoryStockPO> {
}
