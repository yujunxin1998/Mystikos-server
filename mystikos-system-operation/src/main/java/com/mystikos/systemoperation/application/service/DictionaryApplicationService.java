package com.mystikos.systemoperation.application.service;

import com.mystikos.common.dict.DictCatalog;
import com.mystikos.common.dict.DictSource;
import com.mystikos.systemoperation.domain.SystemOperationException;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 聚合全部 {@link DictSource} Bean 的字典数据。各业务模块的枚举是权威来源，
 * 这里只做只读聚合，不落库、不缓存过期问题——每次调用都是即时读取枚举当前状态。
 */
@Service
public class DictionaryApplicationService {

    private final List<DictSource> dictSources;

    public DictionaryApplicationService(List<DictSource> dictSources) {
        this.dictSources = dictSources;
    }

    public List<DictCatalog> listAll() {
        return dictSources.stream()
                .flatMap(source -> source.dictCatalogs().stream())
                .toList();
    }

    public DictCatalog get(String catalogCode) {
        Map<String, DictCatalog> byCode = new LinkedHashMap<>();
        for (DictCatalog catalog : listAll()) {
            byCode.putIfAbsent(catalog.code(), catalog);
        }
        DictCatalog catalog = byCode.get(catalogCode);
        if (catalog == null) {
            throw SystemOperationException.dictCatalogNotFound(catalogCode);
        }
        return catalog;
    }
}
