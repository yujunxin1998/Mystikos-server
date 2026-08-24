-- 补充陪玩测试账号：V17 只建了一个陪玩账号（companion@mystikos.local），且没有打手扩展资料
-- （identity_companion_profile）也没有已发布名片（identity_companion_showcase），没法测分页/
-- 筛选类功能（后台打手列表、老板浏览名片目录 GET /api/v1/companions）。这里补 10 个陪玩账号，
-- 每个都配好打手资料（级别/时薪/接单状态）和已审核通过并发布的名片（简介/一句话标签/可约状态/
-- 游戏标签），可以直接拿来联调，不用先手工走完申请-审核-发布流程。
--
-- 密码统一是 Test@123456，跟 V17 用同一个 BCrypt 哈希（已验证 matches() 通过，见 V17 注释）。
-- 名片没有配照片/视频/语音——种子数据不铺垫 MinIO 对象，预签名链接会指向不存在的对象，
-- 前端展示时按无封面图处理即可（GET /api/v1/companions 卡片的 coverPhotoUrl 允许为空）。
-- 用户ID 6-15、名片记录ID 1001-1010，都是人工挑的小整数，不会跟应用运行时用雪花算法生成的
-- 真实ID冲突（同 V17 的做法）。
-- 跟 V4/V17 一样：这是本地开发/联调用的种子数据，上线前必须删除或改密码。
INSERT INTO identity_user (id, email, password_hash, nickname, privacy_anonymous, status, created_at) VALUES
    (6,  'companion01@mystikos.local', '$2a$10$kORc.uSw0lituVY7XkBu4ez3fFkco2YnGsgCKMtebKi2d34Kt087.', '星野', false, 'ACTIVE', now()),
    (7,  'companion02@mystikos.local', '$2a$10$kORc.uSw0lituVY7XkBu4ez3fFkco2YnGsgCKMtebKi2d34Kt087.', '沐辰', false, 'ACTIVE', now()),
    (8,  'companion03@mystikos.local', '$2a$10$kORc.uSw0lituVY7XkBu4ez3fFkco2YnGsgCKMtebKi2d34Kt087.', '云汐', false, 'ACTIVE', now()),
    (9,  'companion04@mystikos.local', '$2a$10$kORc.uSw0lituVY7XkBu4ez3fFkco2YnGsgCKMtebKi2d34Kt087.', '凛冬', false, 'ACTIVE', now()),
    (10, 'companion05@mystikos.local', '$2a$10$kORc.uSw0lituVY7XkBu4ez3fFkco2YnGsgCKMtebKi2d34Kt087.', '阿飞', false, 'ACTIVE', now()),
    (11, 'companion06@mystikos.local', '$2a$10$kORc.uSw0lituVY7XkBu4ez3fFkco2YnGsgCKMtebKi2d34Kt087.', '小满', false, 'ACTIVE', now()),
    (12, 'companion07@mystikos.local', '$2a$10$kORc.uSw0lituVY7XkBu4ez3fFkco2YnGsgCKMtebKi2d34Kt087.', '夜歌', false, 'ACTIVE', now()),
    (13, 'companion08@mystikos.local', '$2a$10$kORc.uSw0lituVY7XkBu4ez3fFkco2YnGsgCKMtebKi2d34Kt087.', 'Echo', false, 'ACTIVE', now()),
    (14, 'companion09@mystikos.local', '$2a$10$kORc.uSw0lituVY7XkBu4ez3fFkco2YnGsgCKMtebKi2d34Kt087.', '十七', false, 'ACTIVE', now()),
    (15, 'companion10@mystikos.local', '$2a$10$kORc.uSw0lituVY7XkBu4ez3fFkco2YnGsgCKMtebKi2d34Kt087.', '浅眠', false, 'ACTIVE', now())
ON CONFLICT (email) DO NOTHING;

INSERT INTO identity_user_role (user_id, role) VALUES
    (6, 'COMPANION'), (7, 'COMPANION'), (8, 'COMPANION'), (9, 'COMPANION'), (10, 'COMPANION'),
    (11, 'COMPANION'), (12, 'COMPANION'), (13, 'COMPANION'), (14, 'COMPANION'), (15, 'COMPANION')
ON CONFLICT DO NOTHING;

-- 打手扩展资料：级别/时薪/接单状态故意打散，方便测后台列表筛选（GET /api/v1/manage/companions?status=）。
INSERT INTO identity_companion_profile (user_id, level, hourly_rate, companion_status, created_at) VALUES
    (6,  '钻石', 88.00,  'AVAILABLE', now()),
    (7,  '大师', 128.00, 'AVAILABLE', now()),
    (8,  '王者', 108.00, 'BUSY',      now()),
    (9,  '专家', 98.00,  'OFFLINE',   now()),
    (10, '大师', 118.00, 'AVAILABLE', now()),
    (11, '钻石', 78.00,  'AVAILABLE', now()),
    (12, '星耀', 68.00,  'BUSY',      now()),
    (13, '大师', 138.00, 'AVAILABLE', now()),
    (14, '钻石', 88.00,  'OFFLINE',   now()),
    (15, '专家', 98.00,  'AVAILABLE', now())
ON CONFLICT (user_id) DO NOTHING;

-- 名片：直接以 APPROVED 状态入库并发布，跳过草稿->提交->审核流程，reviewer_id 留空
-- （不是走 CompanionShowcaseApplicationService#review 审出来的，没有真实审核人）。
INSERT INTO identity_companion_showcase_revision
    (id, user_id, bio, tagline, availability, status, reviewed_at, created_at, updated_at) VALUES
    (1001, 6,  '王者荣耀国服前50，声音甜美，陪聊陪玩都在线。',       '声音软，操作稳',   '今晚可约', 'APPROVED', now(), now(), now()),
    (1002, 7,  '英雄联盟钻石打野，CS2 也能开黑，脾气好话不多。',     '上分不虚，佛系陪玩', '今晚可约', 'APPROVED', now(), now(), now()),
    (1003, 8,  '和平精英战神段位，苟得住也能刚枪。',               '吃鸡带躺赢',       '周末可约', 'APPROVED', now(), now(), now()),
    (1004, 9,  '原神深渊12星常驻，练度拉满，攻略型陪玩。',          '原神深渊满星',     '暂不接单', 'APPROVED', now(), now(), now()),
    (1005, 10, 'CS2 Faceit 10 级，指挥位，带节奏不摆烂。',         'CS2 稳拿分',      '今晚可约', 'APPROVED', now(), now(), now()),
    (1006, 11, '王者荣耀+和平精英双修，带萌新超有耐心。',           '新手友好，超有耐心', '周末可约', 'APPROVED', now(), now(), now()),
    (1007, 12, '英雄联盟辅助位，声音好听，纯陪聊也可以。',          '声控福音',        '今晚可约', 'APPROVED', now(), now(), now()),
    (1008, 13, '王者/LOL/CS2 都打，全能型陪玩，节奏感强。',        '多游戏通吃',       '今晚可约', 'APPROVED', now(), now(), now()),
    (1009, 14, '原神主玩，偶尔上号打打其他小游戏。',               '原神+其他小众游戏', '暂不接单', 'APPROVED', now(), now(), now()),
    (1010, 15, '和平精英资深陪练，带你上分不虚标。',               '吃鸡陪练首选',     '周末可约', 'APPROVED', now(), now(), now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO identity_companion_showcase_revision_tag (revision_id, tag_id) VALUES
    (1001, 1), (1001, 2),
    (1002, 2), (1002, 5),
    (1003, 3),
    (1004, 4),
    (1005, 5),
    (1006, 1), (1006, 3),
    (1007, 2),
    (1008, 1), (1008, 2), (1008, 5),
    (1009, 4), (1009, 6),
    (1010, 3), (1010, 6)
ON CONFLICT DO NOTHING;

INSERT INTO identity_companion_showcase (user_id, published_revision_id, published_at) VALUES
    (6, 1001, now()), (7, 1002, now()), (8, 1003, now()), (9, 1004, now()), (10, 1005, now()),
    (11, 1006, now()), (12, 1007, now()), (13, 1008, now()), (14, 1009, now()), (15, 1010, now())
ON CONFLICT (user_id) DO NOTHING;
