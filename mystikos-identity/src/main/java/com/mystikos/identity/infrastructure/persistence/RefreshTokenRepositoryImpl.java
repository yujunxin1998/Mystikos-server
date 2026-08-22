package com.mystikos.identity.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mystikos.identity.domain.model.RefreshToken;
import com.mystikos.identity.domain.repository.RefreshTokenRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final RefreshTokenMapper mapper;

    public RefreshTokenRepositoryImpl(RefreshTokenMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenPO po = toPO(refreshToken);
        if (po.getId() == null) {
            mapper.insert(po);
            refreshToken.assignId(po.getId());
        } else {
            mapper.updateById(po);
        }
        return refreshToken;
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        RefreshTokenPO po = mapper.selectOne(
                new LambdaQueryWrapper<RefreshTokenPO>().eq(RefreshTokenPO::getTokenHash, tokenHash));
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public void revokeAllForUser(Long userId) {
        mapper.update(null, new LambdaUpdateWrapper<RefreshTokenPO>()
                .eq(RefreshTokenPO::getUserId, userId)
                .isNull(RefreshTokenPO::getRevokedAt)
                .set(RefreshTokenPO::getRevokedAt, OffsetDateTime.now()));
    }

    private RefreshTokenPO toPO(RefreshToken refreshToken) {
        RefreshTokenPO po = new RefreshTokenPO();
        po.setId(refreshToken.getId());
        po.setUserId(refreshToken.getUserId());
        po.setTokenHash(refreshToken.getTokenHash());
        po.setExpiresAt(refreshToken.getExpiresAt());
        po.setRevokedAt(refreshToken.getRevokedAt());
        return po;
    }

    private RefreshToken toDomain(RefreshTokenPO po) {
        return RefreshToken.restore(po.getId(), po.getUserId(), po.getTokenHash(), po.getExpiresAt(),
                po.getRevokedAt());
    }
}
