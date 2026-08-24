-- 陪玩名片补两个前端编辑器（/companion/card）已经在用的展示字段：一句话标签 + 可约状态。
-- 都是纯展示用的自由文本，不驱动任何业务逻辑（跟接单状态 identity_companion_profile.companion_status
-- 是两回事，那个才是运营/系统认的接单状态），所以不建枚举，跟 bio 一样用自由文本承接。
ALTER TABLE identity_companion_showcase_revision ADD COLUMN tagline VARCHAR(50);
ALTER TABLE identity_companion_showcase_revision ADD COLUMN availability VARCHAR(20);

COMMENT ON COLUMN identity_companion_showcase_revision.tagline IS '一句话标签，展示在名片名称旁，自由文本';
COMMENT ON COLUMN identity_companion_showcase_revision.availability IS '陪玩自报的可约状态文案（如"今晚可约"），纯展示，不是系统接单状态';
