package ir.daneshrefah.scm.cache.client.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties("scm.cache.client")
public class CacheClientProperties {

    /**
     * Backward-compatible switch:
     * true  -> use Hazelcast client (connect to remote scm-cache)
     * false -> start embedded Hazelcast instance
     */
    private boolean distributed = true;

    /**
     * Default backend when cache name is not explicitly configured.
     */
    private CacheType defaultType = CacheType.REMOTE;

    /**
     * Default ttl applied by template operations when ttl is not provided.
     * zero or negative => no ttl.
     */
    private Duration defaultTtl = Duration.ZERO;

    /**
     * Default max size for local (Caffeine) caches.
     */
    private long defaultMaximumSize = 10_000L;

    /**
     * Per cache configuration.
     * Example:
     * scm.cache.client.caches.user_cache.type=near
     */
    private Map<String, CacheDefinition> caches = new HashMap<>();

    @Getter
    @Setter
    public static class CacheDefinition {

        /**
         * Cache backend type for this cache name.
         */
        private CacheType type;

        /**
         * Remote map name for REMOTE/NEAR.
         * If null, cacheName will be used.
         */
        private String remoteName;

        /**
         * ttl override for this cache.
         * zero or negative => no ttl.
         */
        private Duration ttl;

        /**
         * max size override for local caffeine cache.
         */
        private Long maximumSize;
    }
}
