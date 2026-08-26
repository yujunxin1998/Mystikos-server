# 模块结构

目标：MVP 阶段以单一 Spring Boot 可执行 jar 部署，但包结构从第一天起就为"按限界上下文拆分为独立微服务"做准备，拆分时业务代码（domain / application）零改动。

## 1. Maven 多模块结构

```
mystikos-server/
├── mystikos-bom/                    依赖版本仲裁（dependencyManagement）
├── mystikos-common/                 真正跨域公共：统一响应封装（APIResponse）、错误码接口、Money/DateRange 等值对象、工具类（已落地，见 exception-handling.md）
├── mystikos-common-web/             Web 层公共代码：全局异常处理器、业务异常基类，依赖 Spring MVC（已落地，见 exception-handling.md）
├── mystikos-common-security/        Spring Security OAuth2 资源服务器配置、JWT 解析、当前用户上下文
├── mystikos-common-event/           领域事件基类 + 事件总线抽象（现：Spring 事件，未来：MQ 适配层）
│
├── mystikos-identity/               身份与访问
├── mystikos-membership/             会员成长
├── mystikos-provider-catalog/       陪玩服务目录
├── mystikos-booking/                预约撮合
├── mystikos-commerce/               商城（货架/库存/购物车/心愿单/商品订单）
├── mystikos-gifting/                礼物打赏
├── mystikos-relationship/           亲密度
├── mystikos-payment/                支付账本
├── mystikos-leaderboard/            排行榜与统计（CQRS 读模型）
├── mystikos-review/                 评价评分
├── mystikos-trust-safety/           信任与安全
├── mystikos-notification/           通知
├── mystikos-system-operation/       系统运营（枚举字典聚合、系统配置内容、后台操作日志）
│
└── mystikos-app/                    启动壳：聚合所有模块的 Spring Boot 主类，MVP 阶段唯一可执行 jar
```

`mystikos-common` 保持精简，只放真正与业务无关的公共代码；避免它变成"什么都往里塞"的垃圾桶模块——这是共享内核（Shared Kernel）模式最容易腐化的地方。

## 2. 单个业务模块内部结构

以 `mystikos-booking` 为例，五层结构：

```
com.mystikos.booking
├── domain
│   ├── model            BookingOrder 聚合根、TimeRange 值对象、BookingStatus 枚举
│   ├── event             BookingCreatedEvent、BookingConfirmedEvent...
│   ├── repository         仓储接口（领域层只定义接口，不依赖 MyBatis-Plus）
│   └── service             领域服务，如 SlotConflictPolicy
├── application
│   ├── command             CreateBookingCommand、CancelBookingCommand
│   ├── service              BookingApplicationService（用例编排）
│   └── port                  出站端口接口：PaymentPort、CatalogQueryPort（跨上下文调用的接口，由 infrastructure 实现）
├── infrastructure
│   ├── persistence          MyBatis-Plus Mapper / PO、BookingRepositoryImpl
│   ├── acl                   防腐层：调用其他上下文的适配实现，未来替换为 Feign/HTTP 客户端
│   └── config
└── adapter
    └── web                   BookingController，仅暴露本上下文的用例
```

其余业务模块（identity / membership / provider-catalog / commerce / gifting / relationship / payment / leaderboard / review / trust-safety / notification / system-operation）遵循同一模板；`leaderboard` 是纯读侧，没有 `domain/service` 意义上的写聚合，只有 Projection（事件消费 + 定时重算）；`system-operation` 的字典功能同样没有写聚合，直接聚合各上下文实现的 `DictSource` 接口（`mystikos-common` 提供），不查表。

## 3. 跨模块通信规则（决定"能不能真正拆得动"）

1. **跨上下文调用一律经 `application/port` 接口**。单体阶段用 Spring 本地注入实现类；未来拆微服务时把实现从"本地 Bean"替换为 Feign/HTTP/RPC 客户端，domain 和 application 层代码不变。
2. **每个上下文拥有自己的数据库表**（`booking_*`、`commerce_*`、`payment_*` 等前缀区分 schema 归属），**禁止跨上下文外键关联和联表查询**。
3. **跨域数据访问只有两种方式**：
   - 同步查询：通过对方暴露的查询 Port（如 Commerce 查询 Relationship 的亲密度阶段）
   - 异步投影：通过订阅领域事件在本地维护只读副本（如 Leaderboard、Relationship、Membership 消费 `BookingCompleted`/`GiftSent`/`PaymentCaptured`）
4. **领域事件总线现在用 `ApplicationEventPublisher`（进程内）**，未来切换 RocketMQ/Kafka 时只替换 `mystikos-common-event` 里的适配器实现，domain/application 层零改动。
5. **共享能力型上下文（Payment、Notification）依然是独立限界上下文**，不因为被多方调用就弱化其边界——它们有自己的聚合和状态机，只是复用频率更高。

## 4. 数据库拆分约定

- 单一 PostgreSQL 实例，MVP 阶段共用一个 database，按表前缀区分限界上下文归属（`identity_user`、`booking_order`、`commerce_product` 等）。
- **所有 Flyway 迁移脚本集中放在仓库根目录的 `deploy/sql/`**（不按模块拆在各自 `src/main/resources` 下），文件名仍按 Flyway 版本号递增命名（`V1__xxx.sql`、`V2__xxx.sql`……），跨模块共用同一个版本序列——新增迁移前看一眼 `deploy/sql` 目录里当前最大版本号，接着往后编，不要凭空跳号或抢号。逻辑上"每个上下文只改自己前缀的表"这条规则不变，只是物理文件位置和运维习惯（DBA/CI review 一个目录）优先于按模块拆分。`mystikos-app` 的 `application.yml` 用 `spring.flyway.locations: filesystem:${SQL_DIR:deploy/sql}` 指向这个目录，运行/部署时的工作目录要能看到它。
- 拆分为独立微服务时，对应模块的表可以整体迁移到独立 database/实例，因为从一开始就没有跨表外键；届时 `deploy/sql` 里该模块的历史迁移脚本也要一起迁走。

## 5. 从单体到微服务的演进路径

| 阶段 | 部署形态 | 触发条件（需按实际情况判断，非硬指标） |
|---|---|---|
| MVP | `mystikos-app` 单一 jar，所有模块进程内调用 | 起始状态 |
| 拆分候选 | 将耦合度低、独立扩展需求明确的上下文（如 Payment、Notification）拆为独立服务 | 该上下文需要独立发布节奏，或有独立的资源/合规隔离要求 |
| 微服务化 | 按限界上下文拆分为多个独立部署单元，Port 实现从本地 Bean 换成 Feign/HTTP，事件总线换成 MQ | 团队规模、独立发布服务数、峰值 RPS、硬隔离 SLA 需求达到相应门槛（沿用早期预研报告 Gate B 思路） |

拆分顺序建议从依赖方向"叶子节点"开始（如 Notification、Leaderboard 这类只消费事件、少被同步依赖的上下文），最后再考虑 Booking/Commerce/Payment 这类核心交易域，因为它们的拆分对一致性设计（分布式事务/Saga）要求最高。
