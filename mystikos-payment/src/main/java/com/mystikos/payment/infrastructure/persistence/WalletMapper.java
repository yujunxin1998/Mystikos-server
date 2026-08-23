package com.mystikos.payment.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface WalletMapper extends BaseMapper<WalletPO> {

    /**
     * 原子加余额——不读出来改完再写回去，避免并发充值/入账互相覆盖。
     */
    @Update("UPDATE payment_wallet SET balance = balance + #{amount}, updated_at = now() WHERE id = #{id}")
    int creditBalance(@Param("id") Long id, @Param("amount") BigDecimal amount);

    /**
     * 原子扣余额，WHERE 里的 balance >= amount 保证不会扣成负数；受影响行数为 0
     * 就是余额不足，调用方不需要另外查一次余额判断（也没必要——判断完再扣一样有并发窗口）。
     */
    @Update("UPDATE payment_wallet SET balance = balance - #{amount}, updated_at = now() " +
            "WHERE id = #{id} AND balance >= #{amount}")
    int debitBalance(@Param("id") Long id, @Param("amount") BigDecimal amount);
}
