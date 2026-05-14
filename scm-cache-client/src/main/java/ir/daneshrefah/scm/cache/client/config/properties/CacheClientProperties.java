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

    /**
     * Backend selection for utility APIs. LOCAL is process-local; REMOTE is Hazelcast-backed.
     */
    private UtilityBackends utilities = new UtilityBackends();

    /**
     * Optional integrations with Spring ecosystem components.
     */
    private Security security = new Security();

    public enum UtilityBackendType {
        LOCAL,
        REMOTE
    }

    @Getter
    @Setter
    public static class Security {

        private UserCache userCache = new UserCache();
    }

    @Getter
    @Setter
    public static class UserCache {

        /**
         * Enables scm-cache-client backed org.springframework.security.core.userdetails.UserCache.
         */
        private boolean enabled = true;

        /**
         * Spring cache name used for UserDetails entries.
         */
        private String cacheName = "user_cache";

        /**
         * Optional SpEL expression evaluated with #user as UserDetails.
         * Example: #user.username + '::' + #user.user.terminalCode
         */
        private String keyExpression;
    }

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

    @Getter
    @Setter
    public static class UtilityBackends {

        private UtilityBackendType rateLimit = UtilityBackendType.REMOTE;
        private Map<String, UtilityBackendType> rateLimitNames = new HashMap<>();
        private UtilityBackendType lock = UtilityBackendType.REMOTE;
        private Map<String, UtilityBackendType> lockNames = new HashMap<>();
        private UtilityBackendType concurrencyLimit;
        private Map<String, UtilityBackendType> concurrencyLimitNames = new HashMap<>();
        @Deprecated
        private UtilityBackendType semaphore;
        private UtilityBackendType resourceLease = UtilityBackendType.REMOTE;
        private Map<String, UtilityBackendType> resourceLeaseNames = new HashMap<>();

        public UtilityBackendType getConcurrencyLimit() {
            if (concurrencyLimit != null) {
                return concurrencyLimit;
            }
            if (semaphore != null) {
                return semaphore;
            }
            return UtilityBackendType.REMOTE;
        }

        public UtilityBackendType resolveRateLimit(String name) {
            return resolveByName(rateLimitNames, name, rateLimit);
        }

        public UtilityBackendType resolveLock(String name) {
            return resolveByName(lockNames, name, lock);
        }

        public UtilityBackendType resolveConcurrencyLimit(String name) {
            return resolveByName(concurrencyLimitNames, name, getConcurrencyLimit());
        }

        public UtilityBackendType resolveResourceLease(String name) {
            return resolveByName(resourceLeaseNames, name, resourceLease);
        }

        public boolean requiresRemoteRateLimit() {
            return requiresRemote(rateLimit, rateLimitNames);
        }

        public boolean requiresRemoteLock() {
            return requiresRemote(lock, lockNames);
        }

        public boolean requiresRemoteConcurrencyLimit() {
            return requiresRemote(getConcurrencyLimit(), concurrencyLimitNames);
        }

        public boolean requiresRemoteResourceLease() {
            return requiresRemote(resourceLease, resourceLeaseNames);
        }

        private UtilityBackendType resolveByName(Map<String, UtilityBackendType> overrides,
                                                 String name,
                                                 UtilityBackendType defaultBackend) {
            UtilityBackendType exact = findExact(overrides, name);
            if (exact != null) {
                return exact;
            }
            String baseName = baseName(name);
            if (!baseName.equals(normalizeName(name))) {
                UtilityBackendType base = findExact(overrides, baseName);
                if (base != null) {
                    return base;
                }
            }
            return defaultBackend == null ? UtilityBackendType.REMOTE : defaultBackend;
        }

        private UtilityBackendType findExact(Map<String, UtilityBackendType> overrides, String name) {
            if (overrides == null || overrides.isEmpty()) {
                return null;
            }
            String normalizedName = normalizeName(name);
            if (normalizedName.isEmpty()) {
                return null;
            }
            for (Map.Entry<String, UtilityBackendType> entry : overrides.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                if (normalizedName.equals(normalizeName(entry.getKey()))) {
                    return entry.getValue();
                }
            }
            return null;
        }

        private boolean requiresRemote(UtilityBackendType defaultBackend, Map<String, UtilityBackendType> overrides) {
            if (defaultBackend == null || defaultBackend == UtilityBackendType.REMOTE) {
                return true;
            }
            if (overrides == null || overrides.isEmpty()) {
                return false;
            }
            return overrides.values().stream().anyMatch(UtilityBackendType.REMOTE::equals);
        }

        private String baseName(String name) {
            String normalizedName = normalizeName(name);
            int separator = normalizedName.indexOf("::");
            if (separator < 0) {
                return normalizedName;
            }
            return normalizedName.substring(0, separator);
        }

        private String normalizeName(String name) {
            return name == null ? "" : name.trim();
        }
    }
}
