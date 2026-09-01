-- 礼物目录：Mystikos 秘典 v1.0，三档共 12 件礼物，价格单位=星辉石（1 星辉石=1 元人民币）
INSERT INTO gifting_catalog_item (id, code, name, icon, price, tier_id, unlock_rule_type, unlock_rule_threshold, active) VALUES
    (1, 'STARDUST_NOTE', '星尘信笺', 'stardust_note', 10, 1, 'NONE', NULL, true),
    (2, 'MOON_SHELL', '月光贝壳', 'moon_shell', 30, 1, 'NONE', NULL, true),
    (3, 'LAVENDER_SACHET', '薰衣草香囊', 'lavender_sachet', 66, 1, 'NONE', NULL, true),
    (4, 'MIST_CRYSTAL', '迷雾水晶', 'mist_crystal', 99, 1, 'NONE', NULL, true),
    (5, 'AMETHYST_SCEPTER', '紫水晶权杖', 'amethyst_scepter', 188, 2, 'NONE', NULL, true),
    (6, 'FATE_COMPASS', '命运罗盘', 'fate_compass', 388, 2, 'NONE', NULL, true),
    (7, 'STAR_TRAIL_NECKLACE', '星轨项链', 'star_trail_necklace', 520, 2, 'NONE', NULL, true),
    (8, 'PHANTOM_NIGHT_ROSE', '幻夜蔷薇', 'phantom_night_rose', 688, 2, 'NONE', NULL, true),
    (9, 'DEEP_SPACE_CODEX', '深空圣典', 'deep_space_codex', 1314, 3, 'NONE', NULL, true),
    (10, 'HOURGLASS_OF_FATE', '时之沙漏', 'hourglass_of_fate', 3344, 3, 'NONE', NULL, true),
    (11, 'ORACLES_EYE', '神谕之瞳', 'oracles_eye', 5200, 3, 'NONE', NULL, true),
    (12, 'MYSTIKOS_CROWN', 'Mystikos 之冠', 'mystikos_crown', 9999, 3, 'NONE', NULL, true)
ON CONFLICT (code) DO NOTHING;

-- 亲密度等级：十级阶梯，终身累计值
INSERT INTO relationship_intimacy_level_definition (id, code, display_name_zh, display_name_en, threshold, perk_description, sort_order) VALUES
    (1, 'VEILED_ENCOUNTER', '初遇', 'Veiled Encounter', 0, '解锁亲密度进度条展示', 1),
    (2, 'FAINT_GLIMMER', '微光', 'Faint Glimmer', 300, '专属聊天气泡样式', 2),
    (3, 'RESONANCE', '共鸣', 'Resonance', 1000, '主页展示专属称号', 3),
    (4, 'STAR_WHISPERER', '星语者', 'Star Whisperer', 3000, '陪玩场次优先预约权', 4),
    (5, 'ARCANE_CONFIDANT', '秘密挚友', 'Arcane Confidant', 8000, '房间专属入场特效', 5),
    (6, 'FATED_BOND', '命运联结', 'Fated Bond', 18000, '解锁定制陪玩内容权益', 6),
    (7, 'SOUL_PACT', '灵魂契约', 'Soul Pact', 38000, '专属语音/视频陪玩时段', 7),
    (8, 'MYSTIC_BELOVED', '神秘挚爱', 'Mystic Beloved', 78000, '受邀参与高级定制活动', 8),
    (9, 'ETERNAL_VOW', '永恒誓约', 'Eternal Vow', 150000, '年度限定实物礼盒', 9),
    (10, 'THE_ONE_OF_MYSTIKOS', 'Mystikos 唯一', 'The One of Mystikos', 300000, '唯一坐席标识 + 专属定制皮肤', 10)
ON CONFLICT (code) DO NOTHING;

-- VIP 等级：八级阶梯，账户维度终身累计消费（不区分具体陪玩对象）
INSERT INTO membership_tier_definition (id, code, display_name, display_name_en, level, cumulative_spend_threshold, perk_description, sort_order) VALUES
    (1, 'VISITOR', '访客', 'Visitor', 0, 0, '基础送礼与浏览权限', 1),
    (2, 'WANDERER', '旅人', 'Wanderer', 1, 98, '账户徽章点亮', 2),
    (3, 'SEEKER', '探索者', 'Seeker', 2, 328, '每日一次免费小礼物', 3),
    (4, 'OCCULTIST', '秘术师', 'Occultist', 3, 998, '专属客服通道', 4),
    (5, 'STARWALKER', '星界行者', 'Starwalker', 4, 2998, '全站陪玩优先预约权', 5),
    (6, 'FATE_MASTER', '命运主宰', 'Fate Master', 5, 9998, '全站专属进场特效', 6),
    (7, 'ORACLE_BEARER', '神谕执行者', 'Oracle Bearer', 6, 29998, '榜单置顶 + 专属活动邀请', 7),
    (8, 'MYSTIKOS_SOVEREIGN', 'Mystikos 尊主', 'Mystikos Sovereign', 7, 99998, '年度定制礼盒 + 一对一专属活动', 8)
ON CONFLICT (code) DO NOTHING;
