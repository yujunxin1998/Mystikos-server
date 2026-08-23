package com.mystikos.app.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j 接口分组，按限界上下文（Bounded Context）分，不是按 URL 前缀/PRD 功能编号——
 * 同一个上下文哪怕在 Controller 层拆了好几个类（比如 mystikos-identity 下的
 * AuthController/ProfileController/UserController/RoleController，分别对应 S1 账号认证、
 * S2 用户资料、运营态用户与角色管理），也归进同一个分组，避免下拉列表里出现一堆同模块的碎分组。
 * 上下文划分与命名对齐 {@code docs/architecture/domain-model.md} 的限界上下文清单，
 * 单个分组内部靠各 Controller 自己的 {@code @Tag} 再分小节。
 *
 * 新模块加了真实接口后，在这里补一个分组即可；不加也不影响接口本身可用，
 * 只是 Knife4j 左上角分组下拉里少一栏，会落进默认分组。
 */
@Configuration
public class SpringDocGroupConfig {

    /** Identity & Access（身份与访问）：账号认证、老板资料、运营态用户与角色管理，见 mystikos-identity。 */
    @Bean
    public GroupedOpenApi identityApi() {
        return GroupedOpenApi.builder()
                .group("Identity & Access · 身份与访问")
                .pathsToMatch("/api/v1/auth/**", "/api/v1/profile/**", "/api/v1/users/**", "/api/v1/roles/**")
                .build();
    }

    /** Booking（预约撮合），见 mystikos-booking。 */
    @Bean
    public GroupedOpenApi bookingApi() {
        return GroupedOpenApi.builder()
                .group("Booking · 预约撮合")
                .pathsToMatch("/api/v1/bookings/**")
                .build();
    }

    /** Gifting（礼物打赏），见 mystikos-gifting。 */
    @Bean
    public GroupedOpenApi giftingApi() {
        return GroupedOpenApi.builder()
                .group("Gifting · 礼物打赏")
                .pathsToMatch("/api/v1/gifts/**")
                .build();
    }

    /** Relationship（亲密度），见 mystikos-relationship。 */
    @Bean
    public GroupedOpenApi relationshipApi() {
        return GroupedOpenApi.builder()
                .group("Relationship · 亲密度")
                .pathsToMatch("/api/v1/relationships/**")
                .build();
    }

    /** Leaderboard & Stats（排行榜），见 mystikos-leaderboard。 */
    @Bean
    public GroupedOpenApi leaderboardApi() {
        return GroupedOpenApi.builder()
                .group("Leaderboard & Stats · 排行榜")
                .pathsToMatch("/api/v1/leaderboard/**")
                .build();
    }

    /** Membership（会员成长），见 mystikos-membership。 */
    @Bean
    public GroupedOpenApi membershipApi() {
        return GroupedOpenApi.builder()
                .group("Membership · 会员成长")
                .pathsToMatch("/api/v1/memberships/**")
                .build();
    }

    /** Commerce（商城）：商品/购物车/心愿单/订单，见 mystikos-commerce。 */
    @Bean
    public GroupedOpenApi commerceApi() {
        return GroupedOpenApi.builder()
                .group("Commerce · 商城")
                .pathsToMatch("/api/v1/products/**", "/api/v1/cart/**", "/api/v1/wishlist/**", "/api/v1/orders/**")
                .build();
    }

    /**
     * 文件与对象存储不是业务限界上下文，是 mystikos-common-storage 提供的通用技术能力
     * （被各业务上下文按需调用），单独归一组，不挂在任何业务上下文名下。
     */
    @Bean
    public GroupedOpenApi commonCapabilityApi() {
        return GroupedOpenApi.builder()
                .group("通用能力 · 文件与对象存储")
                .pathsToMatch("/api/v1/files/**")
                .build();
    }
}
