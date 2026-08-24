# 测试账号一览

> 数据来源：`deploy/sql/V4__seed_super_admin_user.sql`、`deploy/sql/V17__seed_role_test_accounts.sql`、
> `deploy/sql/V27__seed_more_companion_test_accounts.sql`（一次性快照见 `deploy/init.sql`，注意 init.sql
> 是历史某次快照，可能滞后于最新迁移，以 `deploy/sql/V*` 为准）。
> 仅用于本地开发 / 联调环境，**上线前必须删除或修改密码**。

登录标识使用邮箱（系统登录模型仅支持手机号 / 邮箱二选一，无裸用户名概念）。

| 用户 ID | 邮箱（登录账号） | 密码 | 角色 | 昵称 |
|---|---|---|---|---|
| 1 | administrator@mystikos.local | `Mystikos@123` | ADMIN | 超级管理员 |
| 2 | member@mystikos.local | `Test@123456` | MEMBER | 会员测试账号 |
| 3 | companion@mystikos.local | `Test@123456` | COMPANION | 陪玩测试账号（无打手资料/名片） |
| 4 | customerservice@mystikos.local | `Test@123456` | CUSTOMER_SERVICE | 客服测试账号 |
| 5 | assessor@mystikos.local | `Test@123456` | ASSESSOR | 考核官测试账号 |

### 陪玩测试账号（含打手资料 + 已发布名片）

用于联调后台打手列表（`GET /api/v1/manage/companions`）和老板浏览名片目录
（`GET /api/v1/companions`）——每个账号都已配好接单状态/时薪和一张审核通过并发布的名片，
不用先手工走完申请-审核-发布流程。密码同上，统一 `Test@123456`。

| 用户 ID | 邮箱 | 昵称 | 接单状态 | 级别 | 一句话标签 | 可约状态 | 游戏标签 |
|---|---|---|---|---|---|---|---|
| 6 | companion01@mystikos.local | 星野 | AVAILABLE | 钻石 | 声音软，操作稳 | 今晚可约 | 王者荣耀、英雄联盟 |
| 7 | companion02@mystikos.local | 沐辰 | AVAILABLE | 大师 | 上分不虚，佛系陪玩 | 今晚可约 | 英雄联盟、CS2 |
| 8 | companion03@mystikos.local | 云汐 | BUSY | 王者 | 吃鸡带躺赢 | 周末可约 | 和平精英 |
| 9 | companion04@mystikos.local | 凛冬 | OFFLINE | 专家 | 原神深渊满星 | 暂不接单 | 原神 |
| 10 | companion05@mystikos.local | 阿飞 | AVAILABLE | 大师 | CS2 稳拿分 | 今晚可约 | CS2 |
| 11 | companion06@mystikos.local | 小满 | AVAILABLE | 钻石 | 新手友好，超有耐心 | 周末可约 | 王者荣耀、和平精英 |
| 12 | companion07@mystikos.local | 夜歌 | BUSY | 星耀 | 声控福音 | 今晚可约 | 英雄联盟 |
| 13 | companion08@mystikos.local | Echo | AVAILABLE | 大师 | 多游戏通吃 | 今晚可约 | 王者荣耀、英雄联盟、CS2 |
| 14 | companion09@mystikos.local | 十七 | OFFLINE | 钻石 | 原神+其他小众游戏 | 暂不接单 | 原神、其他 |
| 15 | companion10@mystikos.local | 浅眠 | AVAILABLE | 专家 | 吃鸡陪练首选 | 周末可约 | 和平精英、其他 |

## 说明

- **GUEST** 角色故意未建对应账号：它是未登录 / 匿名访问的默认权限集合，不是落库的角色分配（见 `Role` 枚举注释）。
- 密码以 BCrypt（strength 10）哈希存储，均已用项目实际依赖的 `spring-security-crypto` 生成并验证过 `matches()` 通过。
- 邮箱均为 `xxx@mystikos.local` 占位地址，不是真实可达邮箱，仅用于登录。
- 10 个陪玩测试账号的名片没有配照片/视频/语音（种子数据不铺垫 MinIO 对象），列表/详情接口里对应的图片/视频链接字段会是空值，前端按无封面图处理即可。

## 本地基础设施账号（非应用账号）

来自 `deploy/docker/docker-compose.yaml`，供本地联调使用：

| 服务 | 账号 | 密码 |
|---|---|---|
| PostgreSQL | postgres | postgres |
| Redis | - | Mystikos |
| MinIO | minioadmin | minioadmin |
