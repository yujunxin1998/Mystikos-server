package com.mystikos.common.membership;

/**
 * 会员等级契约。{@code User.updateMembershipTier(MembershipTier)} 只依赖这个接口，
 * 不关心梯度具体怎么实现。
 *
 * <p>权威实现是 {@code mystikos-membership} 的 {@code MembershipTierDefinition}——
 * 一张运营可直接增删改的配置表（VIP0-VIP7 见秘典 v1.0），不是枚举；新增/调整等级
 * 门槛或权益文案不需要发版。{@code mystikos-identity} 的
 * {@code MembershipTierUpgradedEventListener}/{@code MembershipTierDowngradedEventListener}
 * 只用事件自带的 code/level 字段构造这个接口的匿名实现，不反查 Membership 内部类型。
 */
public interface MembershipTier {

    /** 用于排序/比较等级高低的数值，业主自行约定单调递增。 */
    int getLevel();

    /** 稳定编码，落库、跨系统引用用这个，不用展示名。 */
    String getCode();

    /** 展示名，如"入门搭子"。 */
    String getDisplayName();
}
