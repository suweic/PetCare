package com.petcare.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.petcare.system.entity.RefreshToken;
import com.petcare.system.mapper.RefreshTokenMapper;
import com.petcare.system.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Refresh Token 服务实现。
 * <p>
 * 实现 Token Rotation + Reuse Detection 安全模式。
 * 数据库表需通过 Flyway V2 迁移创建（见 db/migration/V2__refresh_token.sql）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenMapper refreshTokenMapper;

    /** Refresh Token 有效期（天） */
    private static final int EXPIRE_DAYS = 7;

    @Override
    @Transactional
    public RefreshToken create(Long userId, Integer userType) {
        // 清理用户旧的 Refresh Token（限制每人最多保留5个有效Token）
        LambdaQueryWrapper<RefreshToken> countWrapper = new LambdaQueryWrapper<RefreshToken>()
                .eq(RefreshToken::getUserId, userId)
                .eq(RefreshToken::getRevoked, false)
                .orderByDesc(RefreshToken::getCreateTime);
        var existing = refreshTokenMapper.selectList(countWrapper);
        if (existing.size() >= 5) {
            // 撤销最旧的超出限制的 Token
            existing.stream()
                    .skip(4)
                    .forEach(t -> {
                        t.setRevoked(true);
                        refreshTokenMapper.updateById(t);
                    });
        }

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setUserType(userType);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(EXPIRE_DAYS));
        refreshToken.setRevoked(false);
        refreshToken.setCreateTime(LocalDateTime.now());

        refreshTokenMapper.insert(refreshToken);
        log.debug("创建RefreshToken: userId={}, expiresAt={}", userId, refreshToken.getExpiresAt());
        return refreshToken;
    }

    @Override
    @Transactional
    public RefreshToken validateAndRotate(String token) {
        // 查找Token
        RefreshToken stored = refreshTokenMapper.selectOne(
                new LambdaQueryWrapper<RefreshToken>()
                        .eq(RefreshToken::getToken, token));

        if (stored == null) {
            log.warn("RefreshToken不存在: token前8位={}", token.substring(0, Math.min(8, token.length())));
            return null;
        }

        // 检测重放攻击：已撤销的Token被再次使用
        if (Boolean.TRUE.equals(stored.getRevoked())) {
            log.error("检测到RefreshToken重放攻击！撤销用户所有Token: userId={}", stored.getUserId());
            revokeAll(stored.getUserId());
            return null;
        }

        // 检测过期
        if (stored.isExpired()) {
            stored.setRevoked(true);
            refreshTokenMapper.updateById(stored);
            log.warn("RefreshToken已过期: userId={}", stored.getUserId());
            return null;
        }

        // Token Rotation: 撤销旧Token，创建新Token
        stored.setRevoked(true);
        refreshTokenMapper.updateById(stored);

        RefreshToken newToken = new RefreshToken();
        newToken.setUserId(stored.getUserId());
        newToken.setUserType(stored.getUserType());
        newToken.setToken(UUID.randomUUID().toString());
        newToken.setExpiresAt(LocalDateTime.now().plusDays(EXPIRE_DAYS));
        newToken.setRevoked(false);
        newToken.setCreateTime(LocalDateTime.now());
        refreshTokenMapper.insert(newToken);

        log.debug("RefreshToken轮换成功: userId={}, old={}... -> new={}...",
                stored.getUserId(),
                token.substring(0, Math.min(8, token.length())),
                newToken.getToken().substring(0, 8));
        return newToken;
    }

    @Override
    @Transactional
    public void revokeAll(Long userId) {
        refreshTokenMapper.update(null,
                new LambdaUpdateWrapper<RefreshToken>()
                        .eq(RefreshToken::getUserId, userId)
                        .eq(RefreshToken::getRevoked, false)
                        .set(RefreshToken::getRevoked, true));
        log.info("已撤销用户所有RefreshToken: userId={}", userId);
    }
}
