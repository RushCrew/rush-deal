package com.rushcrew.auth_service.auth.application;

import com.rushcrew.auth_service.auth.application.policy.TokenPolicy;
import com.rushcrew.auth_service.auth.application.port.AccessTokenProvider;
import com.rushcrew.auth_service.auth.application.port.RefreshTokenProvider;
import com.rushcrew.auth_service.auth.application.result.TokenPairResult;
import com.rushcrew.auth_service.auth.application.result.UserInfoResult;
import com.rushcrew.auth_service.auth.domain.entity.RefreshToken;
import com.rushcrew.auth_service.auth.domain.exception.AuthErrorCode;
import com.rushcrew.auth_service.auth.domain.policy.ConcurrentLoginPolicy;
import com.rushcrew.auth_service.auth.domain.repository.RefreshTokenRepository;
import com.rushcrew.auth_service.auth.domain.vo.UserId;
import com.rushcrew.common.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TokenService {

    private final TokenPolicy tokenPolicy;
    private final AccessTokenProvider accessTokenProvider;
    private final RefreshTokenProvider refreshTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistService tokenBlacklistService;
    private final ConcurrentLoginPolicy concurrentLoginPolicy;

    @Transactional
    public TokenPairResult issueTokenPair(
        Long userId,
        String email,
        String role
    ) {
        String accessToken = accessTokenProvider.generateToken(
            userId,
            email,
            role
        );
        String refreshToken = refreshTokenProvider.generateToken(userId);

        RefreshToken token = RefreshToken.create(
            refreshToken,
            UserId.of(userId),
            tokenPolicy.refreshExpirationMillis()
        );

        refreshTokenRepository.save(token);

        // 동시 로그인 정책 적용
        List<String> existingTokens =
            refreshTokenRepository.findAllTokensByUserId(userId);
        List<String> tokensToRevoke = concurrentLoginPolicy.getTokensToRevoke(
            existingTokens
        );
        tokensToRevoke.forEach(refreshTokenRepository::deleteByToken);

        return new TokenPairResult(accessToken, refreshToken);
    }

    @Transactional
    public String refreshAccessToken(
        UserInfoResult user,
        String refreshTokenValue
    ) {
        RefreshToken token = refreshTokenRepository
            .findByToken(refreshTokenValue)
            .orElseThrow(() ->
                new BusinessException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND)
            );

        token.ensureValid();

        return accessTokenProvider.generateToken(
            user.id(),
            user.name(),
            user.role()
        );
    }

    @Transactional
    public void revokeToken(String accessToken, String refreshToken) {
        // 1. Refresh Token 삭제
        Optional.ofNullable(refreshToken).ifPresent(
            refreshTokenRepository::deleteByToken
        );

        // 2. 만료 시간을 추출
        LocalDateTime expiryDate;
        try {
            expiryDate = accessTokenProvider.getExpiryDate(accessToken);
        } catch (Exception e) {
            // 이미 만료되었거나 잘못된 토큰인 경우 무시
            return;
        }

        // 3. 블랙리스트 처리
        tokenBlacklistService.blacklistAccessToken(accessToken, expiryDate);
    }

    /**
     * 모든 토큰 폐기 (전체 로그아웃)
     */
    @Transactional
    public void revokeAllTokens(Long userId, String currentAccessToken) {
        // 1. 해당 유저의 모든 Refresh Token 삭제
        refreshTokenRepository.deleteAllByUserId(userId);

        // 2. 현재 Access Token 및 유저 자체를 블랙리스트 처리 (멱등성 보장)
        LocalDateTime expiryDate;
        try {
            expiryDate = accessTokenProvider.getExpiryDate(currentAccessToken);
        } catch (Exception e) {
            // 토큰 파싱 실패 시 기본값 사용
            expiryDate = LocalDateTime.now().plusHours(1);
        }

        tokenBlacklistService.blacklistAccessToken(
            currentAccessToken,
            expiryDate
        );
        tokenBlacklistService.blacklistUser(userId, expiryDate);
    }

    public Long getUserIdFromRefreshToken(String refreshToken) {
        return refreshTokenProvider.getUserIdFromToken(refreshToken);
    }
}
