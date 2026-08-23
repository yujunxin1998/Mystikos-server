package com.mystikos.gifting.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GiftCatalogItemMapper extends BaseMapper<GiftCatalogItemPO> {
}
