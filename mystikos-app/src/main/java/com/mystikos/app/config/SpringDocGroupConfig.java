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

    /**
     * Identity & Access（身份与访问）：账号认证、老板资料（含标签目录），见 mystikos-identity。
     * 陪玩身份申请/名片相关会员侧+老板侧接口单独拆到 {@link #companionApi()}，见该方法注释；
     * 运营态用户管理（UserController）都挂在 {@code /api/v1/manage/**} 下，归入 {@link #manageApi()}，
     * 不放在这里——避免同一个上下文的接口被拆到两个分组里，具体见该方法的注释。
     */
    @Bean
    public GroupedOpenApi identityApi() {
        return GroupedOpenApi.builder()
                .group("Identity & Access · 身份与访问")
                .pathsToMatch("/api/v1/auth/**", "/api/v1/profile/**", "/api/v1/roles/**", "/api/v1/tags/**")
                .build();
    }

    /**
     * Companion（陪玩）：陪玩身份申请（会员侧提交/自查）、陪玩名片（会员侧编辑草稿/提交审核，
     * 老板侧浏览目录/查看已发布内容），见 mystikos-identity。单独拆出来是因为这三块虽然物理上
     * 和 Identity & Access 同一个模块（mystikos-identity），但业务上是陪玩这条线自己的闭环，
     * 跟账号认证/老板资料没什么关系，混在一起下拉列表会很长。审核态接口（CompanionApplicationAdminController/
     * CompanionShowcaseAdminController）挂在 {@code /api/v1/manage/**} 下，归入 {@link #manageApi()}，不放在这里。
     */
    @Bean
    public GroupedOpenApi companionApi() {
        return GroupedOpenApi.builder()
                .group("Companion · 陪玩")
                .pathsToMatch("/api/v1/companion-applications/**", "/api/v1/companion-showcase/**",
                        "/api/v1/companions/**")
                .build();
    }

    /**
     * 后台管理（Manage）：所有挂在 {@code /api/v1/manage/**} 前缀下的运营/管理台接口，
     * 跨限界上下文统一收口一个分组——这是本文件里唯一按 URL 前缀而不是按限界上下文分组的例外，
     * 目的是让后台管理接口在文档里集中可见，并与 SecurityConfig 里的鉴权前缀对齐。
     * 各上下文的管理态 Controller（如 Identity 的 UserController）自己再用 {@code @Tag} 分小节。
     */
    @Bean
    public GroupedOpenApi manageApi() {
        return GroupedOpenApi.builder()
                .group("后台管理 · Manage")
                .pathsToMatch("/api/v1/manage/**")
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
     * 我的订单（My Orders）：跨 Booking / Commerce 两个限界上下文的查询层聚合接口，
     * 严格来说不属于任何一个单独的限界上下文，单独拆一个分组，见 mystikos-app 的
     * {@code com.mystikos.app.orders} 包。
     */
    @Bean
    public GroupedOpenApi myOrdersApi() {
        return GroupedOpenApi.builder()
                .group("我的订单 · My Orders")
                .pathsToMatch("/api/v1/my-orders/**")
                .build();
    }

    /**
     * 文件/对象存储、行政区划参考数据都不是业务限界上下文，是被各业务上下文按需调用的
     * 通用技术能力（分别来自 mystikos-common-storage、mystikos-common-region），
     * 单独归一组，不挂在任何业务上下文名下。
     */
    @Bean
    public GroupedOpenApi commonCapabilityApi() {
        return GroupedOpenApi.builder()
                .group("通用能力 · 文件存储与行政区划")
                .pathsToMatch("/api/v1/files/**", "/api/v1/regions/**")
                .build();
    }
}
