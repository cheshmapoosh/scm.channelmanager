package ir.daneshrefah.scm.provider.shetab.tcp;

import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import ir.daneshrefah.scm.provider.shetab.iso.ShetabPackagerFactory;
import ir.daneshrefah.scm.provider.shetab.lease.ShetabEndpointLease;
import ir.daneshrefah.scm.provider.shetab.lease.ShetabEndpointLeaseManager;
import lombok.extern.slf4j.Slf4j;
import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.channel.ASCIIChannel;

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
        ChannelSession current = activeSession;

        if (current != null) {
            return current;
        }

        while (running.getAsBoolean()) {
            guard.verify("before lease acquisition");
            ShetabEndpointLease lease = ensureLease(running, guard);

            if (!running.getAsBoolean()) {
                return null;
            }

            guard.verify("before socket creation");

            ISOChannel newChannel = null;

            try {
                newChannel = createChannel(lease);
                guard.verify("after socket creation");

                newChannel.connect();
                guard.verify("after TCP connect");

                PublishResult publishResult = publish(newChannel, lease.endpoint(), running);

                if (publishResult.session() == null) {
                    disconnectQuietly(newChannel);
                    return null;
                }

                if (publishResult.previousSession() != null
                        && publishResult.previousSession().channel() != newChannel) {
                    disconnectQuietly(publishResult.previousSession().channel());
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

                guard.verify("after connection failure");
                sleepBeforeReconnect(deadline, guard);
            }
        }

        return null;
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

    SessionInvalidation invalidate(ChannelSession failedSession, Throwable error, boolean incrementFailureCount) {
        if (failedSession == null) {
            return SessionInvalidation.none();
        }

        ISOChannel channelToClose;
        ShetabEndpointLease leaseToClose = null;
        boolean activeInvalidated = false;

        synchronized (sessionLock) {
            ChannelSession current = activeSession;

            if (current != null
                    && current.generation() == failedSession.generation()
                    && current.channel() == failedSession.channel()) {
                activeSession = null;
                activeInvalidated = true;
                channelToClose = current.channel();
                lastConnectionFailure = error;
                consecutiveResponseTimeoutCount = 0;
                responseTimeoutGeneration = -1L;

                if (incrementFailureCount) {
                    sameEndpointFailureCount++;
                    leaseToClose = releaseLeaseIfLimitReachedLocked();
                }

                sessionLock.notifyAll();
            } else {
                channelToClose = failedSession.channel();
            }
        }

        disconnectQuietly(channelToClose);
        closeLeaseQuietly(leaseToClose);

        if (activeInvalidated) {
            log.trace("Invalidated Shetab session provider={} generation={} endpoint={} cause={}",
                    config.provider(), failedSession.generation(), failedSession.endpoint(), rootMessage(error));
            return SessionInvalidation.active(failedSession.generation());
        }

        log.debug("Ignored stale Shetab session invalidation provider={} generation={} cause={}",
                config.provider(), failedSession.generation(), rootMessage(error));

        return SessionInvalidation.none();
    }

    SessionInvalidation recordResponseTimeout(long generation, Throwable error) {
        ISOChannel channelToClose = null;
        ShetabEndpointLease leaseToClose = null;
        boolean suspect = false;

        synchronized (sessionLock) {
            ChannelSession current = activeSession;

            if (current == null || current.generation() != generation) {
                return SessionInvalidation.none();
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
                sameEndpointFailureCount++;
                consecutiveResponseTimeoutCount = 0;
                responseTimeoutGeneration = -1L;
                leaseToClose = releaseLeaseIfLimitReachedLocked();
                sessionLock.notifyAll();
            }
        }

        disconnectQuietly(channelToClose);
        closeLeaseQuietly(leaseToClose);

        if (!suspect) {
            return SessionInvalidation.none();
        }

        log.warn("Marked Shetab session suspect after response timeouts provider={} generation={} cause={}",
                config.provider(), generation, rootMessage(error));

        return SessionInvalidation.active(generation);
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
        ShetabEndpointLease current = leasedEndpoint;

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
        channelConfig.put("length-digits", String.valueOf(SHETAB_LENGTH_DIGITS));

        asciiChannel.setConfiguration(new SimpleConfiguration(channelConfig));

        return asciiChannel;
    }

    private PublishResult publish(ISOChannel channel, String endpoint, BooleanSupplier running) {
        synchronized (sessionLock) {
            if (!running.getAsBoolean()) {
                return new PublishResult(null, null);
            }

            ChannelSession previous = activeSession;
            ChannelSession session = new ChannelSession(++generationSequence, channel, endpoint);
            activeSession = session;
            lastConnectedAtMillis = System.currentTimeMillis();
            consecutiveResponseTimeoutCount = 0;
            responseTimeoutGeneration = session.generation();
            sessionLock.notifyAll();

            return new PublishResult(session, previous);
        }
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
        return leaseToClose;
    }

    private void sleepBeforeReconnect(Deadline deadline, ConnectionGuard guard) throws InterruptedException {
        guard.verify("before reconnect sleep");

        long remainingMs = deadline.remainingMillisCeiling();

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

    record SessionInvalidation(boolean activeInvalidated, long generation) {
        static SessionInvalidation active(long generation) {
            return new SessionInvalidation(true, generation);
        }

        static SessionInvalidation none() {
            return new SessionInvalidation(false, -1L);
        }
    }

    private record PublishResult(ChannelSession session, ChannelSession previousSession) {
    }

    private record ConnectionFailure(String endpoint, int failureCount) {
    }
}

record ChannelSession(
        long generation,
        ISOChannel channel,
        String endpoint
) {
}
