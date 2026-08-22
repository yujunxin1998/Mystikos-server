package com.mystikos.booking.domain.model;

import java.time.OffsetDateTime;

/**
 * 预约时段。落库时映射为两个 timestamptz 列，数据库层用
 * {@code EXCLUDE USING gist (companion_id WITH =, tstzrange(start, end) WITH &&)}
 * 约束防止同一陪玩同一时段被重复占用（见 db/migration 脚本与 domain-model.md）。
 */
public record TimeRange(OffsetDateTime start, OffsetDateTime end) {

    public TimeRange {
        if (start == null || end == null) {
            throw new IllegalArgumentException("时段起止时间不能为空");
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("结束时间必须晚于开始时间");
        }
    }
}
