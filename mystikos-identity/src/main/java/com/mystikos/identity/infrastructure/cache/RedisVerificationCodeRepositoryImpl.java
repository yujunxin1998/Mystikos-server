package com.mystikos.identity.infrastructure.cache;

import com.mystikos.identity.domain.model.AuthChannel;
import com.mystikos.identity.domain.model.VerificationCode;
import com.mystikos.identity.domain.model.VerificationPurpose;
import com.mystikos.identity.domain.repository.VerificationCodeRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * 验证码是短生命周期、一次性消费的数据，天生适合用 Redis key 的 TTL 存——不用担心表越积越大，
 * 也不用额外写清理任务，过期自动消失。之前是存 Postgres 的
 * {@code identity_verification_code} 表，已经下线（见 deploy/sql 的 DROP TABLE 迁移）。
 *
 * <p>key 是 {@code channel+purpose+identifier} 维度，重新发送直接覆盖旧值——同一维度只保留最新一条，
 * 和之前 Postgres 版本"只取最新一条未消费记录"的语义一致，但不用查询排序，SET 自带这个效果。
 *
 * <p>{@link #findLatestActive} 还原出来的 {@link VerificationCode} 的 expiresAt 只是个占位的未来时间——
 * 真正的过期判断权交给了 Redis 的 key TTL：这里能查到 key 就说明还没过期，
 * 领域对象 {@code consume()} 里的过期检查因此不会触发，只作为兜底防御保留，不是死代码去掉的必要项。
 */
@Repository
public class RedisVerificationCodeRepositoryImpl implements VerificationCodeRepository {

    private static final String KEY_PREFIX = "identity:vcode:";

    private final StringRedisTemplate redisTemplate;

    public RedisVerificationCodeRepositoryImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public VerificationCode save(VerificationCode verificationCode) {
        String key = key(verificationCode.getChannel(), verificationCode.getIdentifier(), verificationCode.getPurpose());
        if (verificationCode.getConsumedAt() != null) {
            // 一次性验证码，消费之后没有再保留的意义，删掉就是"作废"，不用额外维护 consumedAt 状态
            redisTemplate.delete(key);
            return verificationCode;
        }
        Duration ttl = Duration.between(OffsetDateTime.now(), verificationCode.getExpiresAt());
        if (!ttl.isNegative() && !ttl.isZero()) {
            redisTemplate.opsForValue().set(key, verificationCode.getCode(), ttl);
        }
        return verificationCode;
    }

    @Override
    public Optional<VerificationCode> findLatestActive(AuthChannel channel, String identifier,
                                                         VerificationPurpose purpose) {
        String code = redisTemplate.opsForValue().get(key(channel, identifier, purpose));
        return Optional.ofNullable(code)
                .map(c -> VerificationCode.restore(null, channel, identifier, purpose, c,
                        OffsetDateTime.now().plusDays(1), null));
    }

    private String key(AuthChannel channel, String identifier, VerificationPurpose purpose) {
        return KEY_PREFIX + purpose.name() + ":" + channel.name() + ":" + identifier;
    }
}
