package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.cache.starter.config.properties.CacheClientProperties;
import ir.daneshrefah.scm.cache.starter.utility.lock.LockAcquireFailedException;
import ir.daneshrefah.scm.cache.starter.utility.lock.LockExecutionException;
import ir.daneshrefah.scm.cache.starter.utility.lock.LockUtility;
import org.springframework.beans.factory.ObjectProvider;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

/**
 * Fail-fast distributed coordination for task-workflow aggregates.
 */
public class TaskWorkflowDistributedLock {

    private static final String PREFIX = "scm:task-workflow:";

    private final ObjectProvider<LockUtility> lockProvider;
    private final ObjectProvider<CacheClientProperties> propertiesProvider;

    public TaskWorkflowDistributedLock(
            ObjectProvider<LockUtility> lockProvider,
            ObjectProvider<CacheClientProperties> propertiesProvider
    ) {
        this.lockProvider = lockProvider;
        this.propertiesProvider = propertiesProvider;
    }

    public void verifyAvailable(String serviceCode) {
        requiredLock(serviceCode);
        CacheClientProperties properties = requiredProperties(serviceCode);
        if (properties.getUtilities().getLock()
                != CacheClientProperties.UtilityBackendType.REMOTE) {
            throw unavailable(
                    serviceCode,
                    "distributed REMOTE lock backend is required"
            );
        }
    }

    public <T> T withStartLock(
            String serviceCode,
            String scmClientCorrelationId,
            Callable<T> action
    ) {
        String normalizedServiceCode = serviceCode.trim()
                .toUpperCase(Locale.ROOT);
        String lockName = PREFIX + "start:"
                + normalizedServiceCode + ":"
                + sha256(scmClientCorrelationId.trim());
        return execute(serviceCode, lockName, action);
    }

    public <T> T withProcessLock(
            String serviceCode,
            long processId,
            Callable<T> action
    ) {
        return execute(
                serviceCode,
                PREFIX + "process:" + processId,
                action
        );
    }

    private <T> T execute(
            String serviceCode,
            String lockName,
            Callable<T> action
    ) {
        LockUtility lock = requiredLock(serviceCode);
        CacheClientProperties properties = requiredProperties(serviceCode);
        if (properties.getUtilities().resolveLock(lockName)
                != CacheClientProperties.UtilityBackendType.REMOTE) {
            throw unavailable(
                    serviceCode,
                    "lockName=" + lockName
                            + " resolves to a local-only backend"
            );
        }
        try {
            return lock.executeWithLock(lockName, Duration.ZERO, action);
        } catch (LockAcquireFailedException exception) {
            throw new TaskWorkflowExecutionAlreadyInProgressException(lockName);
        } catch (LockExecutionException exception) {
            if (containsInterrupted(exception)) {
                Thread.currentThread().interrupt();
            }
            throw new TaskWorkflowPersistenceException(
                    "TASK_WORKFLOW distributed lock execution failed "
                            + "serviceCode=" + serviceCode
                            + ", lock=" + lockName,
                    exception
            );
        }
    }

    private LockUtility requiredLock(String serviceCode) {
        List<LockUtility> locks = lockProvider.orderedStream().toList();
        if (locks.size() != 1) {
            throw unavailable(
                    serviceCode,
                    "expected exactly one LockUtility; found " + locks.size()
            );
        }
        return locks.getFirst();
    }

    private CacheClientProperties requiredProperties(String serviceCode) {
        List<CacheClientProperties> properties =
                propertiesProvider.orderedStream().toList();
        if (properties.size() != 1) {
            throw unavailable(
                    serviceCode,
                    "expected exactly one CacheClientProperties; found "
                            + properties.size()
            );
        }
        return properties.getFirst();
    }

    private IllegalStateException unavailable(
            String serviceCode,
            String reason
    ) {
        return new IllegalStateException(
                "Active TASK_WORKFLOW serviceCode=" + serviceCode
                        + " requires distributed locking: " + reason
        );
    }

    private boolean containsInterrupted(Throwable failure) {
        Throwable current = failure;
        while (current != null) {
            if (current instanceof InterruptedException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
