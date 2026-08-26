package com.mystikos.systemoperation.domain;

import com.mystikos.common.web.exception.BusinessException;

public class SystemOperationException extends BusinessException {

    public SystemOperationException(SystemOperationResponseCode code) {
        super(code);
    }

    public SystemOperationException(SystemOperationResponseCode code, String message) {
        super(code, message);
    }

    public static SystemOperationException dictCatalogNotFound(String code) {
        return new SystemOperationException(SystemOperationResponseCode.DICT_CATALOG_NOT_FOUND,
                "字典分类不存在：" + code);
    }

    public static SystemOperationException documentNotFound(String code) {
        return new SystemOperationException(SystemOperationResponseCode.DOCUMENT_NOT_FOUND,
                "系统文档不存在：" + code);
    }
}
