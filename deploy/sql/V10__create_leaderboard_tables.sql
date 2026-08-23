CREATE TABLE leaderboard_companion_stat (
    id            BIGINT PRIMARY KEY,
    companion_id  BIGINT NOT NULL,
    charm_value   NUMERIC(14, 2) NOT NULL DEFAULT 0,

    CONSTRAINT leaderboard_companion_stat_companion_unique UNIQUE (companion_id)
);

CREATE TABLE leaderboard_patron_stat (
    id            BIGINT PRIMARY KEY,
    patron_id     BIGINT NOT NULL,
    guard_value   NUMERIC(14, 2) NOT NULL DEFAULT 0,

    CONSTRAINT leaderboard_patron_stat_patron_unique UNIQUE (patron_id)
);

CREATE INDEX idx_leaderboard_companion_stat_charm ON leaderboard_companion_stat (charm_value DESC);
CREATE INDEX idx_leaderboard_patron_stat_guard ON leaderboard_patron_stat (guard_value DESC);

COMMENT ON TABLE leaderboard_companion_stat IS '陪玩魅力值累计（Leaderboard 限界上下文），纯读侧投影，排名查询时实时排序，不落快照';
COMMENT ON COLUMN leaderboard_companion_stat.id IS '代理主键ID';
COMMENT ON COLUMN leaderboard_companion_stat.companion_id IS '陪玩用户ID';
COMMENT ON COLUMN leaderboard_companion_stat.charm_value IS '累计魅力值，目前只由 Gifting 的赠礼金额累加驱动';

COMMENT ON TABLE leaderboard_patron_stat IS '老板守护值累计（Leaderboard 限界上下文），结构对称，说明同上';
COMMENT ON COLUMN leaderboard_patron_stat.id IS '代理主键ID';
COMMENT ON COLUMN leaderboard_patron_stat.patron_id IS '老板用户ID';
COMMENT ON COLUMN leaderboard_patron_stat.guard_value IS '累计守护值，目前只由 Gifting 的赠礼金额累加驱动';
