package com.mystikos.common.result;

/**
 * 响应码约定。各限界上下文通过实现该接口的枚举扩展自己的错误码，
 * 无需修改 mystikos-common 里的公共代码。
 *
 * 错误码分段约定见 docs/architecture/exception-handling.md。
 */
public interface IResponseCode {

    int getCode();

    String getMessage();
}
