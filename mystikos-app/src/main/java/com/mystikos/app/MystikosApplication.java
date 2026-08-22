package com.mystikos.app;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MVP 阶段唯一的可执行入口：把所有限界上下文模块聚合成一个单体部署。
 * 拆分微服务时，每个模块会有自己独立的 XxxApplication，这个类届时收窄或拆分。
 *
 * annotationClass 限定只把标了 @Mapper 的接口当作 MyBatis Mapper 代理——
 * 不加这个限定，MapperScan 会把 com.mystikos 下所有接口（包括
 * DomainEventPublisher、BookingRepository 这类普通领域接口）都代理成 Bean，
 * 和它们真正的 Spring 实现类冲突（曾经因此炸过：两个 DomainEventPublisher Bean）。
 */
@SpringBootApplication(scanBasePackages = "com.mystikos")
@MapperScan(basePackages = "com.mystikos", annotationClass = Mapper.class)
public class MystikosApplication {

    public static void main(String[] args) {
        SpringApplication.run(MystikosApplication.class, args);
    }
}
