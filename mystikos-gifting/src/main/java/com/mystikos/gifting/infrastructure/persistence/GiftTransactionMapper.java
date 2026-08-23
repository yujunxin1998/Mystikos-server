package com.mystikos.gifting.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface GiftTransactionMapper extends BaseMapper<GiftTransactionPO> {

    @Select("SELECT COALESCE(SUM(quantity), 0) FROM gifting_transaction WHERE patron_id = #{patronId} AND gift_id = #{giftId}")
    long sumQuantityByPatronAndGift(@Param("patronId") Long patronId, @Param("giftId") Long giftId);

    @Select("SELECT COALESCE(SUM(amount), 0) FROM gifting_transaction WHERE patron_id = #{patronId}")
    BigDecimal sumAmountByPatron(@Param("patronId") Long patronId);
}
