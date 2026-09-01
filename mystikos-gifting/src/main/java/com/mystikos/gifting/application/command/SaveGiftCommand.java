package com.mystikos.gifting.application.command;

import com.mystikos.gifting.domain.model.UnlockRuleType;

import java.math.BigDecimal;

/**
 * 新增/更新礼物目录条目共用同一个命令——{@code id} 为空是新增，非空是整行覆盖更新。
 * 目录是运营配置数据，没有部分字段更新的需求。
 */
public record SaveGiftCommand(
        Long id,
        String code,
        String name,
        String icon,
        BigDecimal price,
        Long tierId,
        UnlockRuleType unlockRuleType,
        BigDecimal unlockRuleThreshold,
        boolean active
) {
}
