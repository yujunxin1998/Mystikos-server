package com.mystikos.commerce.domain.model;

/** 地址类型，决定 {@link PatronAddress} 哪些字段必填，见该类的 validate()。 */
public enum AddressType {
    DOMESTIC,
    OVERSEAS
}
