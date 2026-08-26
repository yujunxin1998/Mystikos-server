package com.mystikos.common.dict;

import java.util.List;

/**
 * 各限界上下文实现这个接口（注册为 Spring Bean）声明自己要贡献进字典的枚举。
 * {@code mystikos-system-operation} 只依赖这个接口聚合全部 {@code DictSource} Bean，
 * 不做跨模块反射扫描——保持"跨上下文经接口"的约定，也不会在拆分微服务时失效。
 */
public interface DictSource {

    List<DictCatalog> dictCatalogs();
}
