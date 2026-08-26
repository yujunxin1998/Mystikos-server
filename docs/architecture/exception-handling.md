# 统一响应与全局异常处理

代码位置：`mystikos-common`（`com.mystikos.common.result`）+ `mystikos-common-web`（`com.mystikos.common.web.exception`）。

设计参考了 `F:\javaproj\server` 的 `common-base` / `common-web` 分层模式：响应封装和错误码接口不依赖 Spring Web（可以被未来独立微服务的 Feign/HTTP 客户端直接复用），异常处理器单独放一个依赖 Spring MVC 的模块。

## 1. 组成

| 类 | 模块 | 作用 |
|---|---|---|
| `IResponseCode` | mystikos-common | 错误码约定接口：`getCode()` + `getMessage()` |
| `ResponseCode` | mystikos-common | 全局通用错误码枚举（跨所有模块共用的少数几个） |
| `APIResponse<T>` | mystikos-common | 统一响应体：`code` / `message` / `data` |
| `BusinessException` | mystikos-common-web | 业务异常基类，可携带 `IResponseCode` |
| `GlobalExceptionHandler` | mystikos-common-web | `@RestControllerAdvice` 全局兜底 |

## 2. 错误码分段约定

避免各限界上下文的错误码互相冲突（尤其是未来拆微服务后，网关/前端要能从 `code` 唯一识别是哪个域抛出的错误），每个上下文分配独立号段：

| 号段 | 归属 |
|---|---|
| 200 / 400 / 401 / 403 / 404 / 500 | HTTP 语义对齐的通用码（`ResponseCode`，mystikos-common） |
| 1000–1999 | 跨模块通用业务码（参数错误、数据不存在等，`ResponseCode`） |
| 2000–2999 | Identity & Access |
| 3000–3999 | Membership |
| 4000–4999 | Provider Catalog |
| 5000–5999 | Booking |
| 6000–6999 | Commerce |
| 7000–7999 | Gifting |
| 8000–8999 | Relationship |
| 9000–9999 | Payment & Ledger |
| 10000–10999 | Leaderboard & Stats |
| 11000–11999 | Review |
| 12000–12999 | Trust & Safety |
| 13000–13999 | Notification |
| 14000–14999 | System Operation |

业务模块尚未逐个建立，号段先占位；每个模块落地时在自己的 `xxx-domain`（或对应子模块）里新增一个 `ResponseCode` 枚举，实现 `IResponseCode`，只使用分配给自己的号段。

## 3. 扩展方式：新增一个业务模块的错误码

不需要改 `mystikos-common` 里的任何代码。以未来的 `mystikos-booking` 为例：

```java
package com.mystikos.booking.domain;

import com.mystikos.common.result.IResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BookingResponseCode implements IResponseCode {

    SLOT_CONFLICT(5001, "该时段已被预约，请选择其他时间"),
    BOOKING_NOT_FOUND(5002, "预约订单不存在"),
    BOOKING_STATUS_INVALID(5003, "当前订单状态不允许该操作");

    private final int code;
    private final String message;
}
```

对应的异常类（可选，直接抛 `BusinessException` 也可以，但建一个模块内异常类能收敛该模块的静态工厂方法）：

```java
package com.mystikos.booking.domain;

import com.mystikos.common.web.exception.BusinessException;

public class BookingException extends BusinessException {

    public BookingException(BookingResponseCode code) {
        super(code);
    }

    public BookingException(BookingResponseCode code, String message) {
        super(code, message);
    }

    public static BookingException slotConflict() {
        return new BookingException(BookingResponseCode.SLOT_CONFLICT);
    }
}
```

业务代码里直接 `throw BookingException.slotConflict();`，`GlobalExceptionHandler` 会自动捕获并输出：

```json
{"code": 5001, "message": "该时段已被预约，请选择其他时间", "data": null}
```

## 4. `BusinessException` 的两种用法

- `new BusinessException("临时性错误描述")`：没有稳定错误码，`GlobalExceptionHandler` 归类为 `BUSINESS_ERROR(1003)`，消息透传。适合内部校验、还没来得及定义专属码的场景。
- `new BusinessException(someResponseCode)` 或 `new BusinessException(someResponseCode, "动态消息")`：有稳定错误码，前端/网关可以按 `code` 做分支处理（比如 `SLOT_CONFLICT` 弹特定提示、`refresh` 按钮）；消息可以是枚举里的固定文案，也可以在抛出时用运行时上下文覆盖（如"课程名称 'xxx' 已存在"）。

## 5. 模块级别的额外异常处理器（可选）

全局 `GlobalExceptionHandler` 处理 `BusinessException`、参数校验异常、未分类异常三类。如果某个模块要处理自己特有的异常（比如数据库唯一约束异常、第三方 SDK 抛出的受检异常），可以在该模块自己的 `adapter/web` 包下追加一个限定包路径的处理器，与全局处理器并存：

```java
@RestControllerAdvice(basePackages = "com.mystikos.booking")
public class BookingExceptionHandler {

    @ExceptionHandler(DuplicateKeyException.class)
    public APIResponse<Void> handleDuplicateKey(DuplicateKeyException e) {
        return APIResponse.failed(BookingResponseCode.SLOT_CONFLICT);
    }
}
```

Spring 按 `basePackages` 精确匹配后再退回全局 `@RestControllerAdvice`，两者不会重复触发同一个异常。

## 6. 验证

已用 JDK 17 编译通过（`F:\jdk-17.0.12`，本机默认 `java`/`mvn` 走的是 JDK 8，需要显式切换 `JAVA_HOME` 才能构建这个项目）：

```
mvn -q -DskipTests compile
```
