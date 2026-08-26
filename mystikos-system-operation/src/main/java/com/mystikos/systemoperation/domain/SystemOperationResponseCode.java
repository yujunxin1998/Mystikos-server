package com.mystikos.systemoperation.domain;

import com.mystikos.common.result.IResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 号段 14000-14999，见 docs/architecture/exception-handling.md。 */
@Getter
@AllArgsConstructor
public enum SystemOperationResponseCode implements IResponseCode {

    DICT_CATALOG_NOT_FOUND(14001, "字典分类不存在"),
    DOCUMENT_NOT_FOUND(14002, "系统文档不存在");

    private final int code;
    private final String message;
}
