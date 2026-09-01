CREATE TABLE relationship_intimacy_level_definition (
    id                BIGINT PRIMARY KEY,
    code              VARCHAR(32) NOT NULL,
    display_name_zh   VARCHAR(32) NOT NULL,
    display_name_en   VARCHAR(64),
    threshold         NUMERIC(14, 2) NOT NULL,
    perk_description  VARCHAR(255),
    sort_order        INT NOT NULL,

    CONSTRAINT relationship_intimacy_level_code_unique UNIQUE (code)
);

CREATE TABLE relationship_intimacy_daily_accrual (
    id            BIGINT PRIMARY KEY,
    patron_id     BIGINT NOT NULL,
    companion_id  BIGINT NOT NULL,
    stat_date     DATE NOT NULL,
    accrued       NUMERIC(14, 2) NOT NULL DEFAULT 0,

    CONSTRAINT relationship_intimacy_daily_accrual_key_unique UNIQUE (patron_id, companion_id, stat_date)
);

CREATE TABLE relationship_settings (
    id                  BIGINT PRIMARY KEY,
    daily_intimacy_cap  NUMERIC(14, 2) NOT NULL
);

INSERT INTO relationship_settings (id, daily_intimacy_cap) VALUES (1, 20000)
ON CONFLICT (id) DO NOTHING;

COMMENT ON TABLE relationship_intimacy_level_definition IS '亲密度等级定义（Relationship 限界上下文），运营配置数据，取代原来写死在 IntimacyStagePolicy 里的 5 档常量';
COMMENT ON TABLE relationship_intimacy_daily_accrual IS '某老板对某陪玩每天已经计入亲密度的数值，防刷用的计数器，不是业务事实本身';
COMMENT ON TABLE relationship_settings IS '单行配置表，id 固定为 1；daily_intimacy_cap 是防刷用的每日亲密度获取上限';
