package ir.daneshrefah.scm.uaa.service.shahkar.token;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import ir.daneshrefah.scm.uaa.service.shahkar.config.ShahkarProperties;
import ir.daneshrefah.scm.uaa.service.shahkar.domain.ShahkarTokenResponse;
import ir.daneshrefah.scm.uaa.service.shahkar.transport.ShahkarApiClient;
import ir.daneshrefah.scm.uaa.utils.CachedAccessToken;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class ShahkarTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(ShahkarTokenProvider.class);

    private static final String TOKEN_KEY = "ACCESS_TOKEN";
    private static final String LOCK_KEY = "scm-uaa:shahkar:token-lock";

    private final HazelcastInstance hazelcast;
    private final ShahkarApiClient apiClient;
    private final ShahkarProperties props;

    public String getValidAccessToken() {
        long now = System.currentTimeMillis();
        long skewMs = props.getTokenRefreshSkew().toMillis();

        IMap<String, CachedAccessToken> map = hazelcast.getMap(props.getTokenCacheMap());
        CachedAccessToken<String> cached = map.get(TOKEN_KEY);

        if (cached != null && cached.isValid(now, skewMs)) {
            log.debug("Shahkar token cache hit (valid)");
            return (String)cached.token();
        }

        // lock to prevent thundering herd
        //var lock = hazelcast.get
        boolean locked = false;

        try {
            locked = map.tryLock(LOCK_KEY, 10, TimeUnit.SECONDS);
            if (!locked) {
                // best-effort: maybe another node is refreshing; re-check cache
                cached = map.get(TOKEN_KEY);
                if (cached != null && cached.isValid(System.currentTimeMillis(), skewMs)) {
                    return cached.token();
                }
                throw new IllegalStateException("Shahkar token refresh lock timeout");
            }

            // double-check after lock
            cached = map.get(TOKEN_KEY);
            if (cached != null && cached.isValid(System.currentTimeMillis(), skewMs)) {
                return cached.token();
            }

            log.info("Shahkar token cache miss/expired -> fetching new token");
            ShahkarTokenResponse tokenResp = apiClient.fetchToken();

            CachedAccessToken<String> newToken = toCachedToken(tokenResp, props.getTokenTtlFallback());
            // optionally: set Hazelcast entry TTL to match (or slightly less)
            long ttlMs = Math.max(5_000, (newToken.expiresAtEpochMillis() - System.currentTimeMillis()));
            map.set(TOKEN_KEY, newToken, ttlMs, TimeUnit.MILLISECONDS);

            return newToken.token();
        }catch (Exception e){
            log.error("Fail to load shahkar token from  cache");
            throw new RuntimeException(e);
        } finally {
            if (locked) {
                try { map.unlock(LOCK_KEY); } catch (Exception ignore) {}
            }
        }
    }

    private static CachedAccessToken toCachedToken(ShahkarTokenResponse resp, Duration fallbackTtl) {
        if (resp == null || resp.accessToken() == null || resp.accessToken().isBlank()) {
            throw new IllegalStateException("Shahkar token response is empty");
        }

        long now = System.currentTimeMillis();
        long ttlMs;
        if (resp.expiresInSeconds() != null && resp.expiresInSeconds() > 0) {
            ttlMs = resp.expiresInSeconds() * 1000L;
        } else {
            ttlMs = fallbackTtl.toMillis();
        }

        return new CachedAccessToken(resp.accessToken(), now + ttlMs);
    }
}