package ir.daneshrefah.scm.cache.client.config.properties;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CacheClientPropertiesUtilityBackendsTest {

    @Test
    void resolvesBackendOverrideByExactName() {
        CacheClientProperties.UtilityBackends utilities = new CacheClientProperties.UtilityBackends();
        utilities.setRateLimit(CacheClientProperties.UtilityBackendType.REMOTE);
        utilities.getRateLimitNames().put("login", CacheClientProperties.UtilityBackendType.LOCAL);

        assertEquals(CacheClientProperties.UtilityBackendType.LOCAL, utilities.resolveRateLimit("login"));
        assertEquals(CacheClientProperties.UtilityBackendType.REMOTE, utilities.resolveRateLimit("otp"));
    }

    @Test
    void resolvesBackendOverrideByBaseNameBeforeKeyPart() {
        CacheClientProperties.UtilityBackends utilities = new CacheClientProperties.UtilityBackends();
        utilities.setLock(CacheClientProperties.UtilityBackendType.REMOTE);
        utilities.getLockNames().put("user-update", CacheClientProperties.UtilityBackendType.LOCAL);

        assertEquals(
                CacheClientProperties.UtilityBackendType.LOCAL,
                utilities.resolveLock("user-update::uid::42")
        );
    }

    @Test
    void detectsRemoteNeedFromDefaultOrNameOverride() {
        CacheClientProperties.UtilityBackends utilities = new CacheClientProperties.UtilityBackends();
        utilities.setLock(CacheClientProperties.UtilityBackendType.LOCAL);
        assertFalse(utilities.requiresRemoteLock());

        utilities.getLockNames().put("cluster-lock", CacheClientProperties.UtilityBackendType.REMOTE);
        assertTrue(utilities.requiresRemoteLock());
    }

    @Test
    void treatsNullDefaultBackendAsRemoteFallback() {
        CacheClientProperties.UtilityBackends utilities = new CacheClientProperties.UtilityBackends();
        utilities.setRateLimit(null);

        assertEquals(CacheClientProperties.UtilityBackendType.REMOTE, utilities.resolveRateLimit("login"));
        assertTrue(utilities.requiresRemoteRateLimit());
    }
}
