package com.mystikos.common.membership;

/**
 * 会员等级契约。具体的等级梯度（有几级、每级叫什么、门槛是多少）业务尚未定义，
 * 这里故意不提供任何实现——不要在框架里编造业务规则。
 *
 * 业务定好梯度后，实现一个枚举（如 {@code DefaultMembershipTier implements MembershipTier}）
 * 或者用配置表 + 数据库驱动的实现都可以，{@code User.updateMembershipTier(MembershipTier)}
 * 只依赖这个接口，不需要跟着改。
 *
 * 未来 mystikos-membership 上下文落地后（累计消费驱动的升级事件），
 * 应该是这个接口权威实现的归属地；见 docs/architecture/domain-model.md 的 Membership 设计。
 */
public interface MembershipTier {

    /** 用于排序/比较等级高低的数值，业主自行约定单调递增。 */
    int getLevel();

    /** 稳定编码，落库、跨系统引用用这个，不用展示名。 */
    String getCode();

    /** 展示名，如"入门搭子"。 */
    String getDisplayName();
}
