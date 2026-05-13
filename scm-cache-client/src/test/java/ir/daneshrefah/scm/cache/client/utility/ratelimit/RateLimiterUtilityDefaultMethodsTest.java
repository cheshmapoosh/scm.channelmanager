package ir.daneshrefah.scm.cache.client.utility.ratelimit;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterUtilityDefaultMethodsTest {

    @Test
    void executeRateLimitedRunsJobWhenAllowed() {
        StubRateLimiterUtility utility = new StubRateLimiterUtility(
                new RateLimitResult("login", "k1", 1, true, true, 10, 0, 0)
        );

        String result = utility.executeRateLimited("login", "k1", () -> "ok");

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
    void executeRateLimitedWrapsCheckedException() {
        StubRateLimiterUtility utility = new StubRateLimiterUtility(
                new RateLimitResult("login", "k1", 1, true, true, 10, 0, 0)
        );

        assertThrows(
                RateLimitExecutionException.class,
                () -> utility.executeRateLimited("login", "k1", () -> {
                    throw new Exception("checked");
                })
        );
    }

    @Test
    void runRateLimitedInvokesFailureConsumer() {
        StubRateLimiterUtility utility = new StubRateLimiterUtility(
                new RateLimitResult("login", "k1", 1, false, true, 0, 1_000_000_000L, 1_000_000_000L)
        );
        AtomicBoolean failureHandled = new AtomicBoolean(false);

        utility.runRateLimited(
                "login",
                "k1",
                () -> {
                },
                exception -> failureHandled.set(true)
        );

        assertTrue(failureHandled.get());
    }

    private static class StubRateLimiterUtility implements RateLimiterUtility {

        private final RateLimitResult result;

        private StubRateLimiterUtility(RateLimitResult result) {
            this.result = result;
        }

        @Override
        public RateLimitResult tryConsume(String bucketName, String key) {
            return result;
        }

        @Override
        public RateLimitResult tryConsume(String bucketName, String key, int tokenCountUsage) {
            return result;
        }
    }
}
