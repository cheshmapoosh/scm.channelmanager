package ir.daneshrefah.scm.cache.client.utility.concurrencylimit;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalConcurrencyLimiterUtilityTest {

    @Test
    void executeWithConcurrencyLimitAcquiresAndReleasesPermit() {
        LocalConcurrencyLimiterUtility utility = new LocalConcurrencyLimiterUtility();

        String result = utility.executeWithConcurrencyLimit("queue-A", 1, Duration.ZERO, () -> {
            assertEquals(0, utility.availableSlots("queue-A"));
            return "done";
        });

        assertEquals("done", result);
        assertEquals(1, utility.availableSlots("queue-A"));
    }

    @Test
    void executeWithConcurrencyLimitThrowsWhenPermitIsNotAvailable() {
        LocalConcurrencyLimiterUtility utility = new LocalConcurrencyLimiterUtility();
        utility.initialize("queue-B", 1);
        assertTrue(utility.tryAcquire("queue-B", 1, Duration.ZERO));

        assertThrows(
                ConcurrencyLimitAcquireTimeoutException.class,
                () -> utility.executeWithConcurrencyLimit("queue-B", 1, Duration.ZERO, () -> "done")
        );

        utility.release("queue-B");
    }
}
