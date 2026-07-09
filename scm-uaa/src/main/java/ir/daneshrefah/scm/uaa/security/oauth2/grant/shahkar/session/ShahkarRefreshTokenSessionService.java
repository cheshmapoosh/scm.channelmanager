package ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar.session;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar.ShahkarGrantAuthenticationToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static ir.daneshrefah.scm.uaa.common.utils.ErrorUtils.throwError;

@Service
public class ShahkarRefreshTokenSessionService {
    static final String TOKEN_CACHE_MAP = "scm-uaa:refresh:token-cache";
    static final long IDLE_TIMEOUT_SECONDS = 900;

    private final IMap<String, ShahkarCachedToken> sessions;
    private final long sessionTtlMillis;

    public ShahkarRefreshTokenSessionService(
            HazelcastInstance hazelcast,
            @Value("${scm.super-app.session-ttl}") Long sessionTtlMillis
    ) {
        Assert.notNull(sessionTtlMillis, "Shahkar session TTL must be configured");
        Assert.isTrue(sessionTtlMillis > 0, "Shahkar session TTL must be positive");
        this.sessions = hazelcast.getMap(TOKEN_CACHE_MAP);
        this.sessionTtlMillis = sessionTtlMillis;
    }

    public ShahkarGrantAuthenticationToken requireValid(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            expired();
        }
        ShahkarCachedToken cached = sessions.get(refreshToken);
        long nowEpochMillis = System.currentTimeMillis();
        Instant now = Instant.ofEpochMilli(nowEpochMillis);
        if (cached == null || cached.token() == null || isExpired(cached, now, nowEpochMillis)) {
            if (cached != null && cached.token() != null) {
                cached.token().setAuthenticated(false);
            }
            sessions.remove(refreshToken);
            expired();
        }

        ShahkarGrantAuthenticationToken token = cached.token();
        token.setLastUsedAt(now);
        long remainingTtl = cached.expiresAtEpochMillis() - nowEpochMillis;
        sessions.set(refreshToken, cached, remainingTtl, TimeUnit.MILLISECONDS);
        return token;
    }

    public void store(String refreshToken, ShahkarGrantAuthenticationToken token) {
        if (!StringUtils.hasText(refreshToken) || token == null || !token.isAuthenticated()) {
            return;
        }
        long expiresAt = System.currentTimeMillis() + sessionTtlMillis;
        sessions.set(
                refreshToken,
                new ShahkarCachedToken(token, expiresAt),
                sessionTtlMillis,
                TimeUnit.MILLISECONDS
        );
    }

    private boolean isExpired(ShahkarCachedToken cached, Instant now, long nowEpochMillis) {
        ShahkarGrantAuthenticationToken token = cached.token();
        return nowEpochMillis >= cached.expiresAtEpochMillis()
                || now.isAfter(token.getLastUsedAt().plusSeconds(IDLE_TIMEOUT_SECONDS))
                || now.isAfter(token.getCreatedAt().plusMillis(sessionTtlMillis));
    }

    private void expired() {
        throwError(Constants.OAUTH2_ERROR_CODE_IS_EXPIRED, Constants.OAUTH2_ERROR_CODE_IS_EXPIRED);
    }
}
