package ir.daneshrefah.scm.provider.rest.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.cache.client.utility.lock.LockAcquireFailedException;
import ir.daneshrefah.scm.cache.client.utility.lock.LockUtility;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerContext;
import ir.daneshrefah.scm.provider.rest.config.RestProviderResolvedConfig;
import ir.daneshrefah.scm.provider.rest.customizer.RestAuthUrlProviderMessageCustomizerConfig;
import ir.daneshrefah.scm.provider.rest.exception.RestProviderAuthException;
import ir.daneshrefah.scm.provider.rest.exception.RestProviderAuthFault;
import ir.daneshrefah.scm.provider.rest.http.RestProviderClientRegistry;
import ir.daneshrefah.scm.provider.rest.metrics.RestProviderMetrics;
import ir.daneshrefah.scm.provider.rest.model.RestProviderRequestSpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RestProviderTokenManagerTest {
    private static final String CACHE_NAME = "rest_provider_token_cache";

    @Test
    void returnsCachedTokenWhenValid() {
        ConcurrentMapCacheManager cacheManager = cacheManager();
        putToken(cacheManager, cacheKey("hps", "mb", "credential-a"), "cached-token", 300);
        CapturingTokenClient client = new CapturingTokenClient();
        RestProviderMetrics metrics = new RestProviderMetrics();
        FakeLockUtility lockUtility = new FakeLockUtility();
        RestProviderTokenManager manager = manager(client, cacheManager, lockUtility, metrics);

        ProviderAuthToken token = manager.resolveToken(config("hps"), authConfig(), context("hps", "mb"));

        assertEquals("cached-token", token.accessToken());
        assertEquals(0, client.calls);
        assertEquals(0, lockUtility.calls);
        assertEquals(1, metrics.provider("hps").tokenCacheHitCount());
    }

    @Test
    void refreshesTokenWhenMissingAndCachesResult() {
        ConcurrentMapCacheManager cacheManager = cacheManager();
        CapturingTokenClient client = new CapturingTokenClient();
        RestProviderMetrics metrics = new RestProviderMetrics();
        FakeLockUtility lockUtility = new FakeLockUtility();
        RestProviderTokenManager manager = manager(client, cacheManager, lockUtility, metrics);

        ProviderAuthToken first = manager.resolveToken(config("hps"), authConfig(), context("hps", "mb"));
        ProviderAuthToken second = manager.resolveToken(config("hps"), authConfig(), context("hps", "mb"));

        assertEquals("token-1", first.accessToken());
        assertEquals("token-1", second.accessToken());
        assertEquals(1, client.calls);
        assertEquals(1, lockUtility.calls);
        assertEquals("provider-token-refresh-lock:hps:default:mb:credential-a", lockUtility.lockName);
        assertTrue(client.requestSpec.skipProviderAuth());
        assertEquals(1, metrics.provider("hps").tokenCacheMissCount());
        assertEquals(1, metrics.provider("hps").tokenLockAcquiredCount());
        assertEquals(1, metrics.provider("hps").tokenRefreshCount());
    }

    @Test
    void refreshesTokenWhenCachedTokenIsExpired() {
        ConcurrentMapCacheManager cacheManager = cacheManager();
        putToken(cacheManager, cacheKey("hps", "mb", "credential-a"), "expired-token", -1);
        CapturingTokenClient client = new CapturingTokenClient();
        RestProviderTokenManager manager = manager(client, cacheManager, new FakeLockUtility(), new RestProviderMetrics());

        ProviderAuthToken token = manager.resolveToken(config("hps"), authConfig(), context("hps", "mb"));

        assertEquals("token-1", token.accessToken());
        assertEquals(1, client.calls);
    }

    @Test
    void doubleChecksCacheAfterLockAndAvoidsDuplicateAuthCall() {
        ConcurrentMapCacheManager cacheManager = cacheManager();
        CapturingTokenClient client = new CapturingTokenClient();
        FakeLockUtility lockUtility = new FakeLockUtility();
        lockUtility.beforeJob = () -> putToken(cacheManager, cacheKey("hps", "mb", "credential-a"), "other-node-token", 300);
        RestProviderTokenManager manager = manager(client, cacheManager, lockUtility, new RestProviderMetrics());

        ProviderAuthToken token = manager.resolveToken(config("hps"), authConfig(), context("hps", "mb"));

        assertEquals("other-node-token", token.accessToken());
        assertEquals(0, client.calls);
        assertEquals(1, lockUtility.calls);
    }

    @Test
    void pollsCacheWhenLockCannotBeAcquired() {
        ConcurrentMapCacheManager cacheManager = cacheManager();
        CapturingTokenClient client = new CapturingTokenClient();
        FakeLockUtility lockUtility = new FakeLockUtility();
        lockUtility.acquire = false;
        lockUtility.beforeFailure = () -> putToken(cacheManager, cacheKey("hps", "mb", "credential-a"), "polled-token", 300);
        RestProviderMetrics metrics = new RestProviderMetrics();
        RestProviderTokenManager manager = manager(client, cacheManager, lockUtility, metrics);

        ProviderAuthToken token = manager.resolveToken(config("hps"), authConfig(), context("hps", "mb"));

        assertEquals("polled-token", token.accessToken());
        assertEquals(0, client.calls);
        assertEquals(1, metrics.provider("hps").tokenLockTimeoutCount());
    }

    @Test
    void throwsProviderFaultWhenLockTimesOutAndCacheRemainsEmpty() {
        ConcurrentMapCacheManager cacheManager = cacheManager();
        FakeLockUtility lockUtility = new FakeLockUtility();
        lockUtility.acquire = false;
        RestProviderTokenManager manager = manager(new CapturingTokenClient(), cacheManager, lockUtility, new RestProviderMetrics());

        RestProviderAuthException exception = assertThrows(RestProviderAuthException.class,
                () -> manager.resolveToken(config("hps"), authConfig(), context("hps", "mb")));

        assertEquals(RestProviderAuthFault.PROVIDER_AUTH_LOCK_TIMEOUT, exception.fault());
    }

    @Test
    void throwsProviderFaultWhenCentralizedCacheUnavailable() {
        RestProviderTokenManager manager = manager(new CapturingTokenClient(), null, new FakeLockUtility(), new RestProviderMetrics());

        RestProviderAuthException exception = assertThrows(RestProviderAuthException.class,
                () -> manager.resolveToken(config("hps"), authConfig(), context("hps", "mb")));

        assertEquals(RestProviderAuthFault.PROVIDER_AUTH_CACHE_ERROR, exception.fault());
    }

    @Test
    void invalidAuthResponseDoesNotExposeTokenMaterialInExceptionMessage() {
        CapturingTokenClient client = new CapturingTokenClient();
        client.responseBody = "{\"unexpected\":\"secret-token-value\"}";
        RestProviderMetrics metrics = new RestProviderMetrics();
        RestProviderTokenManager manager = manager(client, cacheManager(), new FakeLockUtility(), metrics);

        RestProviderAuthException exception = assertThrows(RestProviderAuthException.class,
                () -> manager.resolveToken(config("hps"), authConfig(), context("hps", "mb")));

        assertEquals(RestProviderAuthFault.PROVIDER_AUTH_INVALID_RESPONSE, exception.fault());
        assertFalse(exception.getMessage().contains("secret-token-value"));
        assertEquals(1, metrics.provider("hps").tokenRefreshFailureCount());
    }

    private RestProviderTokenManager manager(
            CapturingTokenClient client,
            CacheManager cacheManager,
            LockUtility lockUtility,
            RestProviderMetrics metrics
    ) {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        if (cacheManager != null) {
            beanFactory.registerSingleton("cacheManager", cacheManager);
        }
        if (lockUtility != null) {
            beanFactory.registerSingleton("lockUtility", lockUtility);
        }
        ObjectProvider<CacheManager> cacheManagerProvider = beanFactory.getBeanProvider(CacheManager.class);
        ObjectProvider<LockUtility> lockUtilityProvider = beanFactory.getBeanProvider(LockUtility.class);
        return new RestProviderTokenManager(client, new ObjectMapper(), cacheManagerProvider, lockUtilityProvider, metrics);
    }

    private ConcurrentMapCacheManager cacheManager() {
        return new ConcurrentMapCacheManager(CACHE_NAME);
    }

    private void putToken(CacheManager cacheManager, String key, String accessToken, int expiresInSeconds) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) {
            throw new IllegalStateException("cache not configured");
        }
        cache.put(key, new RestProviderTokenManager.TokenCacheEntry(
                accessToken,
                "Bearer",
                System.currentTimeMillis() + (expiresInSeconds * 1000L),
                expiresInSeconds
        ));
    }

    private String cacheKey(String provider, String channel, String credentialKey) {
        return "provider-token:" + provider + ":default:" + channel + ":" + credentialKey;
    }

    private ProviderMessageCustomizerContext context(String provider, String channel) {
        RestProviderResolvedConfig config = config(provider);
        return new ProviderMessageCustomizerContext(
                provider,
                "rest",
                "svc",
                "op",
                channel,
                "rest",
                Map.of(),
                config,
                "correlation-1",
                "trace-1"
        );
    }

    private RestProviderResolvedConfig config(String provider) {
        return new RestProviderResolvedConfig(
                provider,
                "rest",
                "https://provider.example",
                3000,
                6000,
                true,
                false,
                RestProviderResolvedConfig.HttpRedirect.NORMAL,
                "POST",
                Map.of(),
                Map.of(),
                java.util.List.of(),
                new RestProviderResolvedConfig.Proxy(null, null, null, null),
                new RestProviderResolvedConfig.Auth(RestProviderResolvedConfig.AuthType.NONE, "Authorization", null, null, null, null, true),
                new RestProviderResolvedConfig.Security(java.util.List.of("authorization"), java.util.List.of("token"), 400),
                new RestProviderResolvedConfig.RateLimit(false, "rest-default", "provider")
        );
    }

    private RestAuthUrlProviderMessageCustomizerConfig authConfig() {
        RestAuthUrlProviderMessageCustomizerConfig config = new RestAuthUrlProviderMessageCustomizerConfig();
        config.setPath("/token");
        config.setMethod("POST");
        config.getCache().setName(CACHE_NAME);
        config.getCache().setKeyPrefix("provider-token");
        config.getCache().setAuthProfile("default");
        config.getCache().setCredentialKey("credential-a");
        config.getCache().setRefreshSkew(java.time.Duration.ofSeconds(30));
        config.getCache().setTtlSkew(java.time.Duration.ofSeconds(5));
        config.getLock().setKeyPrefix("provider-token-refresh-lock");
        config.getLock().setWaitTimeout(java.time.Duration.ofMillis(20));
        config.getLock().setLeaseTime(java.time.Duration.ofSeconds(10));
        config.getLock().setRetryDelay(java.time.Duration.ofMillis(1));
        config.getApply().setLocation("header");
        config.getApply().setName("Authorization");
        config.getApply().setFormat("{tokenType} {accessToken}");
        return config;
    }

    private static ObjectProvider<ExecutorService> executorProvider() {
        return new DefaultListableBeanFactory().getBeanProvider(ExecutorService.class);
    }

    private static final class CapturingTokenClient extends RestProviderClientRegistry {
        private int calls;
        private RestProviderRequestSpec requestSpec;
        private String responseBody;

        private CapturingTokenClient() {
            super(executorProvider());
        }

        @Override
        public ResponseEntity<String> exchange(RestProviderResolvedConfig config, RestProviderRequestSpec requestSpec) {
            this.calls++;
            this.requestSpec = requestSpec;
            String body = responseBody != null
                    ? responseBody
                    : "{\"access_token\":\"token-" + calls + "\",\"token_type\":\"Bearer\",\"expires_in\":300}";
            return ResponseEntity.ok(body);
        }
    }

    private static final class FakeLockUtility implements LockUtility {
        private boolean acquire = true;
        private int calls;
        private String lockName;
        private Runnable beforeJob;
        private Runnable beforeFailure;

        @Override
        public <T> T executeWithLock(String lockName, Duration waitTime, Callable<T> job) {
            this.calls++;
            this.lockName = lockName;
            if (!acquire) {
                if (beforeFailure != null) {
                    beforeFailure.run();
                }
                throw new LockAcquireFailedException(lockName, waitTime);
            }
            if (beforeJob != null) {
                beforeJob.run();
            }
            try {
                return job.call();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
