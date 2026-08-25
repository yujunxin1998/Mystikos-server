package com.mystikos.app.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 全局把 {@code Long}/{@code long} 序列化成 JSON 字符串，不留给各 DTO 自己挑着加
 * {@code @JsonSerialize(using = ToStringSerializer.class)}。
 *
 * <p>本项目的主键/外键（用户ID、订单ID……）大多是雪花算法生成的 64 位整数，普遍有
 * 18-19 位，超过 JS {@code Number.MAX_SAFE_INTEGER}（2^53-1，16 位）。JSON 数字被
 * 前端 {@code JSON.parse} 成 JS number 时会丢精度，表现为最后几位被抹成 0——之前
 * 只在个别接口（{@link com.mystikos.identity.application.service.CompanionShowcasePublicView}
 * 等）踩到才手动补丁，属于按下葫芦浮起瓢。这里统一在 Jackson 层把所有 Long 按字符串
 * 序列化，一次性堵住，不用再逐个接口排查。
 *
 * <p>前端因此要统一把返回体里的这些 ID 字段当字符串处理（回传时按字符串传即可，后端
 * 入参反序列化不受影响，Spring 的字符串转 Long 转换器本来就支持）。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longToStringJacksonCustomizer() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        return builder -> builder.modulesToInstall(modules -> modules.add(module));
    }
}
