package ir.daneshrefah.scm.provider.shetab.tcp;

import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import ir.daneshrefah.scm.provider.shetab.iso.ShetabPackagerFactory;
import ir.daneshrefah.scm.provider.shetab.lease.ShetabEndpointLease;
import ir.daneshrefah.scm.provider.shetab.lease.ShetabEndpointLeaseManager;
import lombok.extern.slf4j.Slf4j;
import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.channel.ASCIIChannel;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import java.util.function.BooleanSupplier;

@Slf4j
final class ShetabChannelSessionManager {
    private static final int SHETAB_LENGTH_DIGITS = 4;
    private static final int SUSPECT_RESPONSE_TIMEOUTS = 3;

    private final ShetabResolvedConfig config;
    private final ShetabPackagerFactory packagerFactory;
    private final ShetabEndpointLeaseManager endpointLeaseManager;
    private final Object sessionLock = new Object();

    private volatile ChannelSession activeSession;
    private volatile ShetabEndpointLease leasedEndpoint = ShetabEndpointLease.none();
    private volatile Throwable lastConnectionFailure;
    private volatile long lastConnectedAtMillis;
    private volatile long lastInvalidatedGeneration = -1L;

    private long generationSequence;
    private long responseTimeoutGeneration = -1L;
    private int sameEndpointFailureCount;
    private int consecutiveResponseTimeoutCount;

    ShetabChannelSessionManager(
            ShetabResolvedConfig config,
            ShetabPackagerFactory packagerFactory,
            ShetabEndpointLeaseManager endpointLeaseManager
    ) {
        this.config = Objects.requireNonNull(config, "config");
        this.packagerFactory = Objects.requireNonNull(packagerFactory, "packagerFactory");
        this.endpointLeaseManager = Objects.requireNonNull(endpointLeaseManager, "endpointLeaseManager");
    }

    ChannelSession ensureConnected(
            Deadline deadline,
            BooleanSupplier running,
            ConnectionGuard guard
    ) throws InterruptedException {
        return ensureConnected(deadline, running, guard, Integer.MAX_VALUE);
    }

    private ChannelSession ensureConnected(
            Deadline deadline,
            BooleanSupplier running,
            ConnectionGuard guard,
            int maxConnectAttempts
    ) throws InterruptedException {
        ChannelSession current = activeSession;

        if (current != null) {
            return current;
        }

        int connectAttempts = 0;
        while (running.getAsBoolean()) {
            guard.verify("before lease acquisition");
            ShetabEndpointLease lease = ensureLease(running, guard);

            if (!running.getAsBoolean()) {
                return null;
            }

            guard.verify("before socket creation");

            ISOChannel newChannel = null;

            try {
                connectAttempts++;
                newChannel = createChannel(lease);
                guard.verify("after socket creation");

                newChannel.connect();

                if (!newChannel.isConnected()) {
                    throw new IOException("Shetab channel did not become connected");
                }

                guard.verify("after TCP connect");

                PublishResult publishResult = publish(newChannel, lease.endpoint(), running);

                if (publishResult.session() == null) {
                    disconnectQuietly(newChannel);
                    ChannelSession published = activeSession;
                    return published != null ? published : null;
                }

                log.trace("Connected Shetab ISOChannel provider={} remoteEndpoint={} generation={}",
                        config.provider(), publishResult.session().endpoint(), publishResult.session().generation());

                return publishResult.session();

            } catch (InterruptedException e) {
                disconnectQuietly(newChannel);
                throw e;

            } catch (Exception e) {
                disconnectQuietly(newChannel);

                if (!running.getAsBoolean()) {
                    return null;
                }

                ConnectionFailure failure = recordConnectionFailure(e);

                log.error("Shetab connection failed provider={} remoteEndpoint={} failureCount={} limit={} retryInMs={}",
                        config.provider(),
                        failure.endpoint(),
                        failure.failureCount(),
                        maxSameEndpointReconnectAttempts(),
                        config.reconnectDelayMs(),
                        e);

                if (connectAttempts >= maxConnectAttempts) {
                    return null;
                }

                guard.verify("after connection failure");
                sleepBeforeReconnect(deadline, guard);
            }
        }

        return null;
    }

    void reconnect(long failedGeneration, BooleanSupplier running) throws InterruptedException {
        synchronized (sessionLock) {
            if (!running.getAsBoolean()
                    || activeSession != null
                    || lastInvalidatedGeneration != failedGeneration) {
                return;
            }
        }

        ChannelSession reconnected = ensureConnected(null, running, step -> {
            synchronized (sessionLock) {
                if (!running.getAsBoolean()
                        || activeSession != null
                        || lastInvalidatedGeneration != failedGeneration) {
                    throw new ShetabReconnectCancelledException();
                }
            }
        }, 1);

        if (reconnected == null
                && running.getAsBoolean()
                && reconnectGenerationIfNeeded() == failedGeneration) {
            Thread.sleep(Math.max(1L, config.reconnectDelayMs()));
        }
    }

    long reconnectGenerationIfNeeded() {
        synchronized (sessionLock) {
            return activeSession == null ? lastInvalidatedGeneration : -1L;
        }
    }

    ChannelSession waitForActiveSession(BooleanSupplier running) throws InterruptedException {
        synchronized (sessionLock) {
            while (running.getAsBoolean() && activeSession == null) {
                sessionLock.wait();
            }

            return activeSession;
        }
    }

    boolean isActive(ChannelSession session) {
        if (session == null) {
            return false;
        }

        ChannelSession current = activeSession;

        return current != null
                && current.generation() == session.generation()
                && current.channel() == session.channel();
    }

    InvalidationResult invalidateIfCurrent(ChannelSession failedSession, Throwable error) {
        if (failedSession == null) {
            return InvalidationResult.notInvalidated(-1L);
        }

        ISOChannel channelToClose = null;

        synchronized (sessionLock) {
            if (!isCurrentSessionLocked(failedSession)) {
                return InvalidationResult.notInvalidated(failedSession.generation());
            }

            activeSession = null;
            channelToClose = failedSession.channel();
            lastConnectionFailure = error;
            lastInvalidatedGeneration = failedSession.generation();
            sameEndpointFailureCount++;
            consecutiveResponseTimeoutCount = 0;
            responseTimeoutGeneration = -1L;
            sessionLock.notifyAll();
        }

        disconnectQuietly(channelToClose);

        log.trace("Invalidated Shetab session provider={} generation={} endpoint={} cause={}",
                config.provider(), failedSession.generation(), failedSession.endpoint(), rootMessage(error));
        return InvalidationResult.invalidated(failedSession.generation());
    }

    InvalidationResult recordResponseTimeout(long generation, Throwable error) {
        ISOChannel channelToClose = null;
        boolean suspect = false;

        synchronized (sessionLock) {
            ChannelSession current = activeSession;

            if (current == null || current.generation() != generation) {
                return InvalidationResult.notInvalidated(generation);
            }

            if (responseTimeoutGeneration != generation) {
                responseTimeoutGeneration = generation;
                consecutiveResponseTimeoutCount = 0;
            }

            consecutiveResponseTimeoutCount++;

            if (consecutiveResponseTimeoutCount >= SUSPECT_RESPONSE_TIMEOUTS) {
                activeSession = null;
                suspect = true;
                channelToClose = current.channel();
                lastConnectionFailure = error;
                lastInvalidatedGeneration = current.generation();
                sameEndpointFailureCount++;
                consecutiveResponseTimeoutCount = 0;
                responseTimeoutGeneration = -1L;
                sessionLock.notifyAll();
            }
        }

        disconnectQuietly(channelToClose);

        if (!suspect) {
            return InvalidationResult.notInvalidated(generation);
        }

        log.warn("Marked Shetab session suspect after response timeouts provider={} generation={} cause={}",
                config.provider(), generation, rootMessage(error));

        return InvalidationResult.invalidated(generation);
    }

    void markValidated(ChannelSession session) {
        if (session == null) {
            return;
        }

        synchronized (sessionLock) {
            ChannelSession current = activeSession;

            if (current == null
                    || current.generation() != session.generation()
                    || current.channel() != session.channel()) {
                return;
            }

            consecutiveResponseTimeoutCount = 0;
            responseTimeoutGeneration = session.generation();
            sameEndpointFailureCount = 0;
            lastConnectionFailure = null;
            lastInvalidatedGeneration = -1L;
        }
    }

    void close() {
        ChannelSession sessionToClose;
        ShetabEndpointLease leaseToClose;

        synchronized (sessionLock) {
            sessionToClose = activeSession;
            activeSession = null;
            leaseToClose = leasedEndpoint;
            leasedEndpoint = ShetabEndpointLease.none();
            consecutiveResponseTimeoutCount = 0;
            responseTimeoutGeneration = -1L;
            sameEndpointFailureCount = 0;
            lastConnectionFailure = null;
            lastInvalidatedGeneration = -1L;
            sessionLock.notifyAll();
        }

        if (sessionToClose != null) {
            log.trace("Closing Shetab socket provider={} endpoint={}",
                    config.provider(), sessionToClose.endpoint());
            disconnectQuietly(sessionToClose.channel());
        }

        closeLeaseQuietly(leaseToClose);
    }

    boolean isHealthy() {
        return activeSession != null && lastConnectionFailure == null;
    }

    Throwable lastConnectionFailure() {
        return lastConnectionFailure;
    }

    long lastConnectedAtMillis() {
        return lastConnectedAtMillis;
    }

    long activeGeneration() {
        ChannelSession session = activeSession;
        return session == null ? -1L : session.generation();
    }

    private ShetabEndpointLease ensureLease(BooleanSupplier running, ConnectionGuard guard) {
        ShetabEndpointLease leaseToClose = null;
        ShetabEndpointLease current;

        synchronized (sessionLock) {
            if (sameEndpointFailureCount >= maxSameEndpointReconnectAttempts() && !isEmptyLease(leasedEndpoint)) {
                leaseToClose = leasedEndpoint;
                leasedEndpoint = ShetabEndpointLease.none();
                sameEndpointFailureCount = 0;
            }
            current = leasedEndpoint;
        }

        closeLeaseQuietly(leaseToClose);

        if (!isEmptyLease(current)) {
            return current;
        }

        ShetabEndpointLease acquired = endpointLeaseManager.acquire(config);

        try {
            guard.verify("after lease acquisition");
        } catch (RuntimeException e) {
            closeLeaseQuietly(acquired);
            throw e;
        }

        ShetabEndpointLease unusedLease = null;

        synchronized (sessionLock) {
            if (!running.getAsBoolean()) {
                unusedLease = acquired;
            } else if (isEmptyLease(leasedEndpoint)) {
                leasedEndpoint = acquired;
                current = acquired;
            } else {
                unusedLease = acquired;
                current = leasedEndpoint;
            }
        }

        closeLeaseQuietly(unusedLease);

        log.trace("Acquired Shetab endpoint lease provider={} endpoint={}",
                config.provider(), current != null ? current.endpoint() : null);

        return current;
    }

    private ISOChannel createChannel(ShetabEndpointLease endpointLease) throws Exception {
        String remoteHost = endpointLease.remoteHost();
        int remotePort = endpointLease.remotePort();

        if (remoteHost == null || remoteHost.isBlank() || remotePort <= 0) {
            throw new IllegalStateException(
                    "Invalid leased endpoint for provider " + config.provider() + ": " + endpointLease.endpoint()
            );
        }

        ASCIIChannel asciiChannel = new ASCIIChannel(remoteHost, remotePort, packagerFactory.create(config));

        Properties channelConfig = new Properties();
        channelConfig.put("host", remoteHost);
        channelConfig.put("port", String.valueOf(remotePort));
        channelConfig.put("timeout", String.valueOf(config.socketTimeoutMs()));
        channelConfig.put("connect-timeout", String.valueOf(config.connectTimeoutMs()));
        channelConfig.put("keep-alive", Boolean.toString(config.keepAlive()));
        channelConfig.put("length-digits", String.valueOf(SHETAB_LENGTH_DIGITS));

        asciiChannel.setConfiguration(new SimpleConfiguration(channelConfig));

        return asciiChannel;
    }

    private PublishResult publish(ISOChannel channel, String endpoint, BooleanSupplier running) {
        synchronized (sessionLock) {
            if (!running.getAsBoolean() || activeSession != null || !channel.isConnected()) {
                return new PublishResult(null);
            }

            ChannelSession session = new ChannelSession(++generationSequence, channel, endpoint);
            activeSession = session;
            lastConnectedAtMillis = System.currentTimeMillis();
            consecutiveResponseTimeoutCount = 0;
            responseTimeoutGeneration = session.generation();
            sessionLock.notifyAll();

            return new PublishResult(session);
        }
    }

    private boolean isCurrentSessionLocked(ChannelSession candidate) {
        return candidate != null
                && activeSession != null
                && activeSession.generation() == candidate.generation()
                && activeSession.channel() == candidate.channel();
    }

    private ConnectionFailure recordConnectionFailure(Throwable error) {
        ShetabEndpointLease leaseToClose;
        String endpoint;
        int failureCount;

        synchronized (sessionLock) {
            lastConnectionFailure = error;
            sameEndpointFailureCount++;
            failureCount = sameEndpointFailureCount;
            endpoint = leasedEndpoint != null ? leasedEndpoint.endpoint() : null;
            leaseToClose = releaseLeaseIfLimitReachedLocked();
        }

        closeLeaseQuietly(leaseToClose);

        return new ConnectionFailure(endpoint, failureCount);
    }

    private ShetabEndpointLease releaseLeaseIfLimitReachedLocked() {
        if (sameEndpointFailureCount < maxSameEndpointReconnectAttempts() || isEmptyLease(leasedEndpoint)) {
            return null;
        }

        ShetabEndpointLease leaseToClose = leasedEndpoint;
        leasedEndpoint = ShetabEndpointLease.none();
        sameEndpointFailureCount = 0;
        return leaseToClose;
    }

    private void sleepBeforeReconnect(Deadline deadline, ConnectionGuard guard) throws InterruptedException {
        guard.verify("before reconnect sleep");

        long remainingMs = deadline == null ? Long.MAX_VALUE : deadline.remainingMillisCeiling();

        if (remainingMs <= 0L) {
            guard.verify("after reconnect sleep");
            return;
        }

        long sleepMs = Math.min(Math.max(1L, config.reconnectDelayMs()), remainingMs);
        Thread.sleep(sleepMs);

        guard.verify("after reconnect sleep");
    }

    private boolean isEmptyLease(ShetabEndpointLease endpointLease) {
        return endpointLease == null
                || endpointLease.endpoint() == null
                || endpointLease.endpoint().isBlank();
    }

    private int maxSameEndpointReconnectAttempts() {
        return Math.max(1, config.sameEndpointReconnectAttempts());
    }

    private void disconnectQuietly(ISOChannel channel) {
        if (channel == null) {
            return;
        }

        try {
            channel.disconnect();
        } catch (Exception ignored) {
        }
    }

    private void closeLeaseQuietly(ShetabEndpointLease endpointLease) {
        if (endpointLease == null || isEmptyLease(endpointLease)) {
            return;
        }

        try {
            endpointLease.close();
        } catch (Exception ignored) {
        }
    }

    private String rootMessage(Throwable e) {
        if (e == null) {
            return "unknown";
        }

        Throwable t = e;
        while (t.getCause() != null) {
            t = t.getCause();
        }

        String message = t.getMessage();
        return t.getClass().getSimpleName() + (message != null ? ": " + message : "");
    }

    @FunctionalInterface
    interface ConnectionGuard {
        void verify(String step);
    }

    record InvalidationResult(boolean invalidated, long generation) {
        static InvalidationResult invalidated(long generation) {
            return new InvalidationResult(true, generation);
        }

        static InvalidationResult notInvalidated(long generation) {
            return new InvalidationResult(false, generation);
        }
    }

    private record PublishResult(ChannelSession session) {
    }

    private record ConnectionFailure(String endpoint, int failureCount) {
    }

    private static final class ShetabReconnectCancelledException extends RuntimeException {
    }
}

record ChannelSession(
        long generation,
        ISOChannel channel,
        String endpoint
) {
}
