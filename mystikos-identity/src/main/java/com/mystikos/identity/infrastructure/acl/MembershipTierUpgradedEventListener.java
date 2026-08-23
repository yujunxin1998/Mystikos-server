package com.mystikos.identity.infrastructure.acl;

import com.mystikos.identity.domain.model.User;
import com.mystikos.identity.domain.repository.UserRepository;
import com.mystikos.membership.domain.event.MembershipTierUpgradedEvent;
import com.mystikos.membership.domain.model.DefaultMembershipTier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 把 Membership 的等级状态投影到 User.membershipTierLevel/Code——这两个字段只是本地只读缓存，
 * 权威数据仍在 mystikos-membership 的 membership_account 表，不能反过来从这边写回去
 * （见 module-structure.md 的跨上下文数据访问规则：异步投影，不共享表）。
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
            DefaultMembershipTier tier = DefaultMembershipTier.valueOf(event.getNewTierCode());
            user.updateMembershipTier(tier);
            userRepository.save(user);
        });
    }
}
