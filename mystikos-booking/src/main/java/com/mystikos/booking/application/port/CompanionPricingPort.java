package com.mystikos.booking.application.port;

/**
 * 出站端口：查询陪玩当前时薪与是否可预约。陪玩单价归属 mystikos-identity 的
 * CompanionProfile（只能后台管理），Booking 不持有定价数据，下单时经这个 Port 权威取价，
 * 不信任客户端传入的价格。MVP 阶段本地 Bean 实现见 infrastructure/acl。
 */
public interface CompanionPricingPort {

    CompanionPricingSnapshot getPricing(Long companionId);
}
