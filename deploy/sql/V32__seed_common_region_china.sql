-- 补种中国（大陆 + 港澳台）到 common_region：国内收货地址（commerce_patron_address，见 V33）
-- 需要省级行政区数据做省份选择器，V15 的欧洲种子数据里没有中国。
-- 编码用标准 ISO 3166-2:CN（省级，34 个：23 省 + 5 自治区 + 4 直辖市 + 2 特别行政区），
-- 层级结构、字段含义与 V15 的写法完全一致，不重复解释。
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('CN', NULL, 'COUNTRY', '中国', 'China', 46);

INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('CN-BJ', 'CN', 'SUBDIVISION', '北京市', 'Beijing', 1),
('CN-TJ', 'CN', 'SUBDIVISION', '天津市', 'Tianjin', 2),
('CN-HE', 'CN', 'SUBDIVISION', '河北省', 'Hebei', 3),
('CN-SX', 'CN', 'SUBDIVISION', '山西省', 'Shanxi', 4),
('CN-NM', 'CN', 'SUBDIVISION', '内蒙古自治区', 'Inner Mongolia', 5),
('CN-LN', 'CN', 'SUBDIVISION', '辽宁省', 'Liaoning', 6),
('CN-JL', 'CN', 'SUBDIVISION', '吉林省', 'Jilin', 7),
('CN-HL', 'CN', 'SUBDIVISION', '黑龙江省', 'Heilongjiang', 8),
('CN-SH', 'CN', 'SUBDIVISION', '上海市', 'Shanghai', 9),
('CN-JS', 'CN', 'SUBDIVISION', '江苏省', 'Jiangsu', 10),
('CN-ZJ', 'CN', 'SUBDIVISION', '浙江省', 'Zhejiang', 11),
('CN-AH', 'CN', 'SUBDIVISION', '安徽省', 'Anhui', 12),
('CN-FJ', 'CN', 'SUBDIVISION', '福建省', 'Fujian', 13),
('CN-JX', 'CN', 'SUBDIVISION', '江西省', 'Jiangxi', 14),
('CN-SD', 'CN', 'SUBDIVISION', '山东省', 'Shandong', 15),
('CN-HA', 'CN', 'SUBDIVISION', '河南省', 'Henan', 16),
('CN-HB', 'CN', 'SUBDIVISION', '湖北省', 'Hubei', 17),
('CN-HN', 'CN', 'SUBDIVISION', '湖南省', 'Hunan', 18),
('CN-GD', 'CN', 'SUBDIVISION', '广东省', 'Guangdong', 19),
('CN-GX', 'CN', 'SUBDIVISION', '广西壮族自治区', 'Guangxi', 20),
('CN-HI', 'CN', 'SUBDIVISION', '海南省', 'Hainan', 21),
('CN-CQ', 'CN', 'SUBDIVISION', '重庆市', 'Chongqing', 22),
('CN-SC', 'CN', 'SUBDIVISION', '四川省', 'Sichuan', 23),
('CN-GZ', 'CN', 'SUBDIVISION', '贵州省', 'Guizhou', 24),
('CN-YN', 'CN', 'SUBDIVISION', '云南省', 'Yunnan', 25),
('CN-XZ', 'CN', 'SUBDIVISION', '西藏自治区', 'Tibet', 26),
('CN-SN', 'CN', 'SUBDIVISION', '陕西省', 'Shaanxi', 27),
('CN-GS', 'CN', 'SUBDIVISION', '甘肃省', 'Gansu', 28),
('CN-QH', 'CN', 'SUBDIVISION', '青海省', 'Qinghai', 29),
('CN-NX', 'CN', 'SUBDIVISION', '宁夏回族自治区', 'Ningxia', 30),
('CN-XJ', 'CN', 'SUBDIVISION', '新疆维吾尔自治区', 'Xinjiang', 31),
('CN-TW', 'CN', 'SUBDIVISION', '台湾省', 'Taiwan', 32),
('CN-HK', 'CN', 'SUBDIVISION', '香港特别行政区', 'Hong Kong', 33),
('CN-MO', 'CN', 'SUBDIVISION', '澳门特别行政区', 'Macau', 34);
