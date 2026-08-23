# Mystikos Server

Mystikos 陪玩公会平台的后端服务。项目采用 Spring Boot 3 + Maven 多模块结构，以领域驱动设计（DDD）组织业务代码，当前以模块化单体形式构建和部署。

## 技术栈

- JDK 17
- Spring Boot 3.2.5
- Spring Security + JWT
- MyBatis-Plus / PageHelper
- PostgreSQL 17 + Flyway
- Redis 7.2
- MinIO
- Knife4j / OpenAPI 3
- Maven

## 项目结构

```text
Mystikos-server/
├── mystikos-app/                 # 应用启动模块，聚合所有业务模块
├── mystikos-common*/             # Web、事件、安全、缓存、存储、地区等通用能力
├── mystikos-identity/            # 认证、用户、角色、陪玩资料
├── mystikos-booking/             # 预约
├── mystikos-membership/          # 会员成长
├── mystikos-commerce/            # 商品、购物车、心愿单、订单
├── mystikos-gifting/             # 礼物与打赏
├── mystikos-relationship/        # 亲密度
├── mystikos-leaderboard/         # 排行榜
├── mystikos-provider-catalog/    # 服务者目录（骨架）
├── mystikos-payment/             # 支付（骨架）
├── mystikos-review/              # 评价（骨架）
├── mystikos-trust-safety/        # 信任与安全（骨架）
├── mystikos-notification/        # 通知（骨架）
├── deploy/sql/                   # 本地 Flyway 增量迁移脚本
├── deploy/init.sql               # 部署环境数据库初始化快照
├── deploy/docker/                # 本地依赖服务编排
└── docs/                         # 架构与领域设计文档
```

## 本地启动

### 环境要求

- JDK 17
- Maven 3.9+
- Docker Desktop 与 Docker Compose

### 1. 启动依赖服务

项目默认依赖 PostgreSQL、Redis 和 MinIO：

```powershell
docker compose -f deploy\docker\docker-compose.yaml up -d
```

当前 Compose 文件使用了 `F:/javaproj/Mystikos/local-docker` 下的 Windows bind mount。若项目位于其他目录，请先修改 `deploy/docker/docker-compose.yaml` 中的挂载路径，并准备 Redis 配置文件。完整说明见 [本地 Docker 依赖文档](deploy/docker/README.md)。

### 2. 启动应用

在 `Mystikos-server` 目录执行：

```powershell
mvn -pl mystikos-app -am spring-boot:run
```

默认 Maven `local` profile 包含 Flyway，应用启动时会自动执行 `deploy/sql` 中尚未应用的迁移。

如需启用本地邮件验证码投递，可启动 Mailpit，并激活 Spring 的 `local` profile：

```powershell
docker run -d --name mystikos-mailpit -p 1025:1025 -p 8025:8025 axllent/mailpit:v1.27
mvn -pl mystikos-app -am spring-boot:run -Dspring-boot.run.profiles=local
```

Mailpit Web UI 地址为 <http://localhost:8025>。

### 3. 访问服务

| 服务 | 地址 |
|---|---|
| API 服务 | <http://localhost:8099> |
| Knife4j 接口文档 | <http://localhost:8099/doc.html> |
| OpenAPI JSON | <http://localhost:8099/v3/api-docs> |
| MinIO 控制台 | <http://localhost:9001> |

## 构建与运行

### 本地构建

```powershell
mvn clean package
java -jar mystikos-app\target\mystikos-app.jar
```

### 部署构建

```powershell
mvn clean package -Pdeploy
```

`deploy` Maven profile 不包含 Flyway。部署前需按目标环境的数据库变更流程初始化或升级数据库；全新环境可参考 `deploy/init.sql`。请勿修改已在共享环境执行过的 Flyway 迁移文件，应新增更高版本的迁移。

### 构建应用镜像

先完成部署构建，再将可执行 JAR 放入 Docker 构建上下文：

```powershell
Copy-Item mystikos-app\target\mystikos-app.jar .\mystikos-app.jar
docker build -t mystikos-server:local .
```

镜像运行时需要把 JAR 挂载到 `/app/mystikos-app.jar`，并通过环境变量或 `/config/` 下的外部配置连接基础设施服务。

## 常用配置

默认配置位于 `mystikos-app/src/main/resources/application.yml`，本地 Spring profile 配置位于 `application-local.yml`。常用环境变量如下：

| 环境变量 | 用途 | 本地默认值 |
|---|---|---|
| `REDIS_HOST` / `REDIS_PORT` | Redis 地址 | `localhost` / `6379` |
| `REDIS_PASSWORD` | Redis 密码 | `Mystikos` |
| `MINIO_ENDPOINT` | MinIO API 地址 | `http://localhost:9000` |
| `MINIO_ACCESS_KEY` / `MINIO_SECRET_KEY` | MinIO 凭证 | `minioadmin` / `minioadmin` |
| `JWT_SECRET` | JWT 签名密钥 | 仅提供本地占位值 |
| `MAIL_ENABLED` | 是否真实发送邮件 | `false` |
| `DISCORD_CLIENT_ID` / `DISCORD_CLIENT_SECRET` | Discord OAuth 凭证 | 无可用于生产的默认值 |
| `DISCORD_REDIRECT_URI` | 后端 Discord OAuth 回调地址，由部署环境配置 | 空 |
| `OAUTH_FRONTEND_RETURN_URI` | OAuth 完成后携带一次性票据跳回的前端地址 | 空 |
| `SQL_DIR` | Flyway 脚本目录 | `deploy/sql` |

数据库连接默认使用 `jdbc:postgresql://localhost:5432/mystikos`，用户名和密码均为 `postgres`。真实密钥和生产凭证必须通过环境变量或外部配置注入，不应提交到仓库。

## 开发约定

- API 统一使用 `/api/v1` 前缀，并通过统一响应结构返回结果。
- 业务模块按 adapter、application、domain、infrastructure 分层。
- 跨领域写入通过领域事件衔接，避免直接操作其他模块的数据表。
- `mystikos-app` 是唯一启动入口；新增模块需同时加入根 `pom.xml` 和启动模块依赖。
- 新增或修改接口后，应在 Knife4j 中确认分组、鉴权声明和请求模型。

## 文档

- [设计文档索引](docs/README.md)
- [技术选型](docs/architecture/tech-stack.md)
- [领域模型](docs/architecture/domain-model.md)
- [模块结构](docs/architecture/module-structure.md)
- [统一响应与异常处理](docs/architecture/exception-handling.md)
- [PRD 对照与模块取舍](docs/architecture/prd-alignment.md)
