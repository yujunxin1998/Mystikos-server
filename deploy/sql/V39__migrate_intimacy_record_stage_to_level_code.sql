ALTER TABLE relationship_intimacy_record ADD COLUMN level_code VARCHAR(32);

-- 老的 5 档裸整数序号（0-4）按等价含义映射到新十级阶梯里最接近的一档；
-- 这批数据本身就是标注为占位值的演示数据（见 V9 的表注释），生产环境如果已有真实
-- 累计进度，需要按 relationship_intimacy_level_definition 的真实门槛重新解析，不能照抄这个映射。
UPDATE relationship_intimacy_record SET level_code = CASE stage
    WHEN 0 THEN 'VEILED_ENCOUNTER'
    WHEN 1 THEN 'RESONANCE'
    WHEN 2 THEN 'FATED_BOND'
    WHEN 3 THEN 'SOUL_PACT'
    WHEN 4 THEN 'ETERNAL_VOW'
    ELSE 'VEILED_ENCOUNTER'
END;

ALTER TABLE relationship_intimacy_record ALTER COLUMN level_code SET NOT NULL;
ALTER TABLE relationship_intimacy_record DROP COLUMN stage;

COMMENT ON COLUMN relationship_intimacy_record.level_code IS '亲密度等级编码，引用 relationship_intimacy_level_definition(code)；用字符串而不是裸整数序号，插入新等级不会导致后面的等级错位';
