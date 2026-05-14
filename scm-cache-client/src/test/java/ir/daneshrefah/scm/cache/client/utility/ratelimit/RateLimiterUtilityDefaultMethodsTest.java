package ir.daneshrefah.scm.cache.client.utility.ratelimit;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterUtilityDefaultMethodsTest {

    @Test
    void executeRateLimitedRunsJobWhenAllowed() {
        StubRateLimiterUtility utility = new StubRateLimiterUtility(
                new RateLimitResult("login", "k1", 1, true, true, 10, 0, 0)
        );

        String result = utility.executeRateLimited("login", "k1", () -> "ok", exception -> "fallback");

        assertEquals("ok", result);
    }

    @Test
    void executeRateLimitedUsesFailureCallbackWhenRejected() {
        StubRateLimiterUtility utility = new StubRateLimiterUtility(
                new RateLimitResult("login", "k1", 1, false, true, 0, 1_000_000_000L, 1_000_000_000L)
        );

        String result = utility.executeRateLimited(
                "login",
                "k1",
                () -> "ok",
                exception -> "fallback"
        );

        assertEquals("fallback", result);
    }

    @Test
    void executeRateLimitedDoesNotRunJobWhenRejected() {
        StubRateLimiterUtility utility = new StubRateLimiterUtility(
                new RateLimitResult("login", "k1", 1, false, true, 0, 1_000_000_000L, 1_000_000_000L)
        );
        AtomicBoolean executed = new AtomicBoolean(false);

        String result = utility.executeRateLimited("login", "k1", () -> {
            executed.set(true);
            return "ok";
        }, exception -> "fallback");

        assertEquals("fallback", result);
        assertFalse(executed.get());
    }

    @Test
    void executeRateLimitedPassesRequestedTokenCount() {
        StubRateLimiterUtility utility = new StubRateLimiterUtility(
                new RateLimitResult("login", "k1", 3, true, true, 7, 0, 0)
        );

        String result = utility.executeRateLimited("login", "k1", 3, () -> "ok", exception -> "fallback");

        assertEquals("ok", result);
        assertEquals(3, utility.lastRequestedTokens);
    }

    @Test
    void executeRateLimitedWrapsCheckedException() {
        StubRateLimiterUtility utility = new StubRateLimiterUtility(
                new RateLimitResult("login", "k1", 1, true, true, 10, 0, 0)
        );

        assertThrows(
                RateLimitExecutionException.class,
                () -> utility.executeRateLimited("login", "k1", () -> {
                    throw new Exception("checked");
                }, exception -> "fallback")
        );
    }

    @Test
    void executeRateLimitedInvokesFailureCallbackWithRateLimitException() {
        StubRateLimiterUtility utility = new StubRateLimiterUtility(
                new RateLimitResult("login", "k1", 1, false, true, 0, 1_000_000_000L, 1_000_000_000L)
        );
        AtomicBoolean failureHandled = new AtomicBoolean(false);

        utility.executeRateLimited(
                "login",
                "k1",
                () -> "ok",
                exception -> {
                    failureHandled.set(true);
                    return "fallback";
                }
        );

        assertTrue(failureHandled.get());
    }

    private static class StubRateLimiterUtility implements RateLimiterUtility {

        private final RateLimitResult result;
        private int lastRequestedTokens;

        private StubRateLimiterUtility(RateLimitResult result) {
            this.result = result;
        }

        @Override
        public RateLimitResult tryConsume(String bucketName, String key, int tokenCountUsage) {
            this.lastRequestedTokens = tokenCountUsage;
            return result;
        }
    }
}
