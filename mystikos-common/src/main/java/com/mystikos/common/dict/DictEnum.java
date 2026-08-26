package com.mystikos.common.dict;

/**
 * 需要暴露给前端的枚举实现这个接口，由 {@code mystikos-system-operation} 的
 * 字典接口统一聚合展示——枚举本身仍是权威来源，字典只做只读聚合，不重复建表存一份，
 * 避免前端硬编码的中文文案跟后端枚举各改各的、互相漂移。
 */
public interface DictEnum {

    String getCode();

    String getDisplayName();
}
