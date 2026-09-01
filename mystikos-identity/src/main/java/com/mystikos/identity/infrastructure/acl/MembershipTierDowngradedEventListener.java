package com.mystikos.identity.infrastructure.acl;

import com.mystikos.common.membership.MembershipTier;
import com.mystikos.identity.domain.model.User;
import com.mystikos.identity.domain.repository.UserRepository;
import com.mystikos.membership.domain.event.MembershipTierDowngradedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 赠礼退款导致的降级同样要同步进 User 的本地投影，否则 User.membershipTierLevel/Code
 * 会一直停留在退款前的等级——和 {@link MembershipTierUpgradedEventListener} 分开监听
 * 只是为了对称两个事件类型各自独立，处理逻辑本身完全一致。
 */
@Component
public class MembershipTierDowngradedEventListener {

    private final UserRepository userRepository;

    public MembershipTierDowngradedEventListener(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(MembershipTierDowngradedEvent event) {
        userRepository.findById(event.getPatronId()).ifPresent(user -> {
            user.updateMembershipTier(new EventBackedMembershipTier(event.getNewTierCode(), event.getNewTierLevel()));
            userRepository.save(user);
        });
    }

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
