package com.mystikos.identity.infrastructure.acl;

import com.mystikos.common.membership.MembershipTier;
import com.mystikos.identity.domain.model.User;
import com.mystikos.identity.domain.repository.UserRepository;
import com.mystikos.membership.domain.event.MembershipTierUpgradedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 把 Membership 的等级状态投影到 User.membershipTierLevel/Code——这两个字段只是本地只读缓存，
 * 权威数据仍在 mystikos-membership 的 membership_account 表，不能反过来从这边写回去
 * （见 module-structure.md 的跨上下文数据访问规则：异步投影，不共享表）。
 *
 * <p>只依赖事件自带的 code/level 字段，不反查 Membership 的具体等级类型——VIP 梯度已经
 * 从枚举换成配置表驱动，事件本身就是权威的数据来源，Identity 不需要（也不应该）知道
 * Membership 内部用什么类型表示一档等级。
 */
@Component
public class MembershipTierUpgradedEventListener {

    private final UserRepository userRepository;

    public MembershipTierUpgradedEventListener(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(MembershipTierUpgradedEvent event) {
        userRepository.findById(event.getPatronId()).ifPresent(user -> {
            user.updateMembershipTier(new EventBackedMembershipTier(event.getNewTierCode(), event.getNewTierLevel()));
            userRepository.save(user);
        });
    }

    /** 只为了满足 User.updateMembershipTier(MembershipTier) 的入参类型，displayName 不落库不使用。 */
    private record EventBackedMembershipTier(String code, int level) implements MembershipTier {
        @Override
        public int getLevel() {
            return level;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getDisplayName() {
            return code;
        }
    }
}
