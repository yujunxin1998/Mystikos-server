# 依赖服务的 Docker 定义

`mystikos-app` 本身不在这里定义镜像——应用是 `mvn package` 出来的可执行 jar，
这里只管它依赖的三个基础设施服务（数据库、缓存、对象存储）在本地/自建环境怎么起。

**推荐用 compose 一次起三个服务**（同目录 `docker-compose.yaml`，镜像版本/挂载路径/环境变量跟下面的 `docker run` 命令逐条对应）：

```powershell
docker compose -f deploy\docker\docker-compose.yaml up -d
```

`btree_gist` 扩展也自动装好了（`postgres-init\01-btree-gist.sql` 挂到 PostgreSQL 官方镜像认的 `/docker-entrypoint-initdb.d/`，只在数据目录第一次 initdb 时自动跑一次）——**前提是 `local-docker\postgres\data` 是全新空目录**；如果你之前用手动 `docker run` 起过库、目录已经初始化过，这个脚本不会重跑，仍需按下面 PostgreSQL 一节手动执行一次 `CREATE EXTENSION`。

下面每个服务一条独立的 `docker run` 命令原样保留，作为不想用 compose 时的手动等价物，也是理解每个服务具体怎么配置的文档。

## 数据/配置挂载位置

三个服务的持久化数据都挂载到仓库外的 `F:\javaproj\Mystikos\local-docker\`（和 `Mystikos-server` 平级，不在 git 仓库里，不会被误提交）：

```
F:\javaproj\Mystikos\local-docker\
├─ postgres\data\           -> 容器内 /var/lib/postgresql/data
├─ redis\
│  ├─ config\redis.conf     -> 容器内 /usr/local/etc/redis/redis.conf（手动维护，仓库里没有副本）
│  └─ data\                 -> 容器内 /data
└─ minIo\data\              -> 容器内 /data
```

目录已建好（除 `redis\config\redis.conf` 是有内容的配置文件，其余都是空目录，容器首次启动会自己初始化）。用的是 bind mount（宿主机真实路径）而不是 Docker 具名卷，方便直接在文件管理器里看数据、备份、或者删掉整个 `local-docker` 目录重置环境。

**为什么只有 Redis 单独挂了一个 config 目录**：三个服务的"配置"实际落地方式不一样——
- **Redis** 官方镜像默认不认目录里的配置文件，得显式 `redis-server /path/to/redis.conf` 才会读，所以单独给它挂一个 `config\redis.conf`（内容就是之前拼在命令行里的 `--appendonly yes`，挪出来是为了以后加参数不用改 `docker run` 命令）。
- **PostgreSQL** 的配置文件（`postgresql.conf`/`pg_hba.conf`）是 initdb 时自动生成在 `PGDATA` 里的，而 `PGDATA` 就是我们挂的那个 `postgres\data\` 目录——容器第一次启动之后，这两个文件会直接出现在 `postgres\data\` 里，不需要单独挂 config，改完文件重启容器生效。
- **MinIO** 没有传统意义上的配置文件，账号密码走环境变量（`docker run` 里的 `MINIO_ROOT_USER`/`MINIO_ROOT_PASSWORD`），bucket 策略/IAM 这些运行时状态存在 `.minio.sys` 里，也在已经挂载的 `minIo\data\` 目录下，同样不需要单独的 config 目录。

**Windows 提醒**：Docker Desktop 默认只共享 `C:` 盘给容器，用 `F:` 盘路径做 bind mount 之前，需要先在 Docker Desktop → Settings → Resources → File sharing 里把 `F:` 盘加进去，否则容器起不来会报 `mounting "/run/desktop/mnt/host/f/..." to rootfs` 之类的错误。

## 镜像版本对照表

| 服务 | 镜像 | 版本 | 选型说明 |
|---|---|---|---|
| PostgreSQL | `postgres` | `17-alpine` | 应用端 JDBC 驱动是 42.7.2，对服务端版本没有硬要求；17 是当前稳定、有两年补丁验证的大版本，比刚出的 18 更保守。需要 `btree_gist` 扩展（预约时段防重的 `EXCLUDE USING gist` 约束用），镜像里默认自带，见下方启动后的一条 `CREATE EXTENSION` |
| Redis | `redis` | `7.2-alpine` | **协议选型经过确认**：Redis 从 7.4 起改成 RSAL/SSPL 双协议，8.0 又加了 AGPLv3——AGPLv3 有"集成方也要公开源码"的义务，对本项目这种商业化 SaaS 有法律风险。7.2 是最后一个 BSD-3-Clause 协议的版本，功能对我们当前用法（缓存 + 未来的限流/验证码 TTL）完全够用，选它规避协议风险。如果后续要用 8.x 的新特性，需要先评估 AGPL 条款再升级 |
| MinIO | `minio/minio` | `RELEASE.2025-09-07T16-13-09Z-cpuv1` | **已知情况**：MinIO 官方从 2025 年 10 月起不再对外发布免费 Docker 镜像，Docker Hub / Quay 仓库定格在这一版，后续安全补丁不会再发到这个镜像。当前先用它把本地开发跑起来；生产上线前需要重新评估（自行从源码构建、或迁移到 Chainguard 等第三方维护的镜像 `cgr.dev/chainguard/minio:latest`），已记入 P0 上线门待办 |

## 启动命令（`docker run`）

命令用 PowerShell 语法写（本机主 shell）；Git Bash 下用的话把反引号续行符换成 `\`、路径换成 `/f/javaproj/Mystikos/local-docker/...` 即可，效果一样。

### PostgreSQL

```powershell
docker run -d `
  --name mystikos-postgres `
  -p 5432:5432 `
  -e POSTGRES_DB=mystikos `
  -e POSTGRES_USER=postgres `
  -e POSTGRES_PASSWORD=postgres `
  -v F:\javaproj\Mystikos\local-docker\postgres\data:/var/lib/postgresql/data `
  postgres:17-alpine
```

`EXCLUDE USING gist` 约束依赖的 `btree_gist` 扩展不是默认启用的，容器起来之后建一次库（只需一次，`CREATE EXTENSION IF NOT EXISTS` 是幂等的）：

```powershell
docker exec -it mystikos-postgres psql -U postgres -d mystikos -c "CREATE EXTENSION IF NOT EXISTS btree_gist;"
```

对应 `mystikos-app/application.yml` 里的默认值（`localhost:5432` / `postgres` / `postgres`），本地起完直接能跑 Flyway。

### Redis

```powershell
docker run -d `
  --name mystikos-redis `
  -p 6379:6379 `
  -v F:\javaproj\Mystikos\local-docker\redis\config\redis.conf:/usr/local/etc/redis/redis.conf `
  -v F:\javaproj\Mystikos\local-docker\redis\data:/data `
  redis:7.2-alpine `
  redis-server /usr/local/etc/redis/redis.conf
```

对应 `application.yml` 的默认值（`localhost:6379`，无密码）。配置文件里 `appendonly yes` 开的是 AOF 持久化，避免容器重启后缓存全丢——本地开发其实无所谓，但顺手带上，生产环境必须开。要调参数（比如 `maxmemory`、`requirepass`）直接改 `local-docker\redis\config\redis.conf`，重启容器（`docker restart mystikos-redis`）生效，不用碰 `docker run` 命令。

### MinIO

```powershell
docker run -d `
  --name mystikos-minio `
  -p 9000:9000 `
  -p 9001:9001 `
  -e MINIO_ROOT_USER=minioadmin `
  -e MINIO_ROOT_PASSWORD=minioadmin `
  -v F:\javaproj\Mystikos\local-docker\minIo\data:/data `
  minio/minio:RELEASE.2025-09-07T16-13-09Z-cpuv1 `
  server /data --console-address ":9001"
```

对应 `application.yml` 的默认值（`http://localhost:9000`，`minioadmin`/`minioadmin`，bucket `mystikos`）。控制台在 `http://localhost:9001`，用同一对账号密码登录。bucket 不用手动建——`mystikos-common-storage` 的 `MinioConfig` 启动时会自动检查/创建。

**注意**：应用启动时会主动连接 MinIO 检查 bucket，MinIO 没起会导致 `mystikos-app` 启动失败（和 Postgres 一样是硬依赖）；Redis 用的是 Lettuce 客户端懒连接，没起不会阻塞启动，只在真正用到缓存时才会报错。

### Mailpit（本地 SMTP）

```powershell
docker run -d `
  --name mystikos-mailpit `
  -p 1025:1025 `
  -p 8025:8025 `
  axllent/mailpit:v1.27
```

使用 `local` profile 启动应用后，验证码邮件会发送到 Mailpit。浏览器打开 `http://localhost:8025` 查看，邮件不会真实投递：

```powershell
mvn -pl mystikos-app -am spring-boot:run -Dspring-boot.run.profiles=local
```

阿里云短信在本地默认仍为模拟模式。需要真实联调时，再设置下列环境变量；发送会产生费用：

```powershell
$env:ALIYUN_SMS_ENABLED="true"
$env:ALIBABA_CLOUD_ACCESS_KEY_ID="你的 RAM AccessKey ID"
$env:ALIBABA_CLOUD_ACCESS_KEY_SECRET="你的 RAM AccessKey Secret"
$env:ALIYUN_SMS_SIGN_NAME="已审核的短信签名"
$env:ALIYUN_SMS_TEMPLATE_CODE="SMS_123456789"
```

## 生产环境提醒（P0 上线门，暂不处理）

- Postgres/Redis/MinIO 的密码全是本地开发占位值，上线前必须换成从密钥管理服务读取的真实密钥（呼应 `docs/architecture/domain-model.md` 第 11 节）。
- MinIO 镜像来源需要在上线前重新评估（见上表说明）。
- 三个服务目前都是单实例、无高可用编排，仅适合本地开发/联调。
