package ir.daneshrefah.scm.cache.client.utility.lock;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalLockUtilityTest {

    @Test
    void executeWithLockRunsJobAndReturnsResult() {
        LocalLockUtility utility = new LocalLockUtility();

        String result = utility.executeWithLock("lock-A", Duration.ZERO, () -> "done");

        assertEquals("done", result);
    }

    @Test
    void wrapsCheckedJobException() {
        LocalLockUtility utility = new LocalLockUtility();

        assertThrows(
                LockExecutionException.class,
                () -> utility.executeWithLock("lock-B", Duration.ZERO, () -> {
                    throw new IOException("boom");
                })
        );
    }
}
