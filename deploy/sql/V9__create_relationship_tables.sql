CREATE TABLE relationship_intimacy_record (
    id                    BIGINT PRIMARY KEY,
    patron_id             BIGINT NOT NULL,
    companion_id          BIGINT NOT NULL,
    stage                 INT NOT NULL DEFAULT 0,
    progress_value        NUMERIC(14, 2) NOT NULL DEFAULT 0,
    last_interaction_at   TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT relationship_intimacy_record_pair_unique UNIQUE (patron_id, companion_id)
);

CREATE INDEX idx_relationship_intimacy_record_patron ON relationship_intimacy_record (patron_id);
CREATE INDEX idx_relationship_intimacy_record_companion ON relationship_intimacy_record (companion_id);

COMMENT ON TABLE relationship_intimacy_record IS '老板×陪玩亲密度记录（Relationship 限界上下文），代理主键 + (patron_id, companion_id) 唯一约束模拟复合业务键';
COMMENT ON COLUMN relationship_intimacy_record.id IS '代理主键ID';
COMMENT ON COLUMN relationship_intimacy_record.patron_id IS '老板用户ID';
COMMENT ON COLUMN relationship_intimacy_record.companion_id IS '陪玩用户ID';
COMMENT ON COLUMN relationship_intimacy_record.stage IS '亲密度阶段：0-4，阈值定义见 IntimacyStagePolicy（占位值，业务确认后可改）';
COMMENT ON COLUMN relationship_intimacy_record.progress_value IS '累计互动进度值，目前只由 Gifting 的赠礼金额累加驱动';
COMMENT ON COLUMN relationship_intimacy_record.last_interaction_at IS '最后互动时间';
