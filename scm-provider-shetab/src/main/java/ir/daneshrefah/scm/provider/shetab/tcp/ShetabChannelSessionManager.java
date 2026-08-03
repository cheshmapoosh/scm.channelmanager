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
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static ir.daneshrefah.scm.provider.shetab.tcp.ShetabConnectionSnapshot.ConnectionState;
import static ir.daneshrefah.scm.provider.shetab.tcp.ShetabConnectionSnapshot.LeaseState;

@Slf4j
final class ShetabChannelSessionManager {
    private static final int SHETAB_LENGTH_DIGITS = 4;
    private static final int REPEATED_FAILURE_SUMMARY_INTERVAL = 10;

    private final ShetabResolvedConfig config;
    private final ShetabPackagerFactory packagerFactory;
    private final ShetabEndpointLeaseManager endpointLeaseManager;
    private final Object stateLock = new Object();
    private final ArrayDeque<ChannelSession> sessionsToClose = new ArrayDeque<>();

    private volatile ChannelSession activeSession;
    private volatile ShetabConnectionSnapshot snapshot;
    private volatile boolean running;

    private ShetabEndpointLease leasedEndpoint = ShetabEndpointLease.none();
    private boolean releaseLeaseRequired;
    private long generationSequence;
    private long retryNotBeforeNanos;
    private Thread connectionWorker;
    private Consumer<SessionInvalidation> invalidationListener = ignored -> {
    };
    private String repeatedFailureSignature;
    private int repeatedFailureCount;

    ShetabChannelSessionManager(
            ShetabResolvedConfig config,
            ShetabPackagerFactory packagerFactory,
            ShetabEndpointLeaseManager endpointLeaseManager
    ) {
        this.config = Objects.requireNonNull(config, "config");
        this.packagerFactory = Objects.requireNonNull(packagerFactory, "packagerFactory");
        this.endpointLeaseManager = Objects.requireNonNull(endpointLeaseManager, "endpointLeaseManager");
        this.snapshot = ShetabConnectionSnapshot.initial(config.provider(), leasingRequired());
    }

    void setInvalidationListener(Consumer<SessionInvalidation> listener) {
        invalidationListener = Objects.requireNonNull(listener, "listener");
    }

    void startWorker() {
        synchronized (stateLock) {
            if (running) {
                return;
            }
            running = true;
            connectionWorker = new Thread(this::connectionWorkerLoop,
                    "scm-shetab-connection-" + config.provider());
            connectionWorker.setDaemon(true);
            connectionWorker.start();
        }
    }

    void stopWorker() {
        Thread worker;
        synchronized (stateLock) {
            if (!running && connectionWorker == null) {
                return;
            }
            running = false;
            worker = connectionWorker;
            stateLock.notifyAll();
        }

        if (worker != null) {
            worker.interrupt();
            if (worker != Thread.currentThread()) {
                try {
                    worker.join(Math.max(1_000L, config.connectTimeoutMs() + 1_000L));
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    ShetabConnectionSnapshot snapshot() {
        return snapshot;
    }

    ChannelSession admitCurrentSession() {
        synchronized (stateLock) {
            ChannelSession current = activeSession;
            if (running && current != null) {
                return current;
            }
        }
        signalRecovery("SESSION_INVALIDATED");
        return null;
    }

    ChannelSession waitForActiveSession(BooleanSupplier clientRunning) throws InterruptedException {
        synchronized (stateLock) {
            while (clientRunning.getAsBoolean() && running && activeSession == null) {
                stateLock.wait();
            }
            return activeSession;
        }
    }

    boolean isActive(ChannelSession session) {
        synchronized (stateLock) {
            return isCurrentSessionLocked(session);
        }
    }

    boolean beginSendIfCurrent(ChannelSession session, BooleanSupplier beginSend) {
        Objects.requireNonNull(beginSend, "beginSend");
        synchronized (stateLock) {
            // Linearizes active-session validity with the tracker's QUEUED -> SENDING claim.
            return running && isCurrentSessionLocked(session) && beginSend.getAsBoolean();
        }
    }

    InvalidationResult invalidateIfCurrent(
            ChannelSession failedSession,
            Throwable failure,
            String phase,
            String reasonCode
    ) {
        if (failedSession == null) {
            return InvalidationResult.notInvalidated(-1L);
        }

        SessionInvalidation invalidation;
        synchronized (stateLock) {
            if (!isCurrentSessionLocked(failedSession)) {
                return InvalidationResult.notInvalidated(failedSession.generation());
            }
            invalidation = invalidateLocked(failedSession, failure, phase, reasonCode);
        }
        notifyInvalidation(invalidation);
        return InvalidationResult.invalidated(failedSession.generation());
    }

    InvalidationResult recordResponseTimeout(long generation, Throwable failure) {
        ChannelSession current;
        synchronized (stateLock) {
            current = activeSession;
        }
        if (current == null || current.generation() != generation) {
            return InvalidationResult.notInvalidated(generation);
        }

        return invalidateIfCurrent(current, failure, "REQUEST", "REQUEST_TIMEOUT_AFTER_SEND");
    }

    void markValidated(ChannelSession session) {
        if (session == null) {
            return;
        }
        synchronized (stateLock) {
            if (!isCurrentSessionLocked(session)) {
                return;
            }
            transitionLocked("TCP_RESPONSE_VERIFIED", "RECEIVE", null, builder -> {
                builder.verified = true;
                builder.lastValidatedAt = Instant.now();
                builder.sameEndpointFailureCount = 0;
            });
        }
    }

    void signalRecovery(String reasonCode) {
        synchronized (stateLock) {
            if (!running || activeSession != null) {
                return;
            }
            transitionLocked(reasonCode, "CONNECTION", null, builder -> builder.recoveryRequired = true);
            stateLock.notifyAll();
        }
    }

    private void connectionWorkerLoop() {
        boolean unexpectedTermination = false;
        Throwable terminalFailure = null;
        synchronized (stateLock) {
            transitionLocked("APPLICATION_READY", "LIFECYCLE", null, builder -> {
                builder.connectionWorkerAlive = true;
                builder.recoveryRequired = true;
            });
        }
        log.info("event=SHETAB_CONNECTION_WORKER_STARTED provider={} reasonCode=APPLICATION_READY threadName={}",
                config.provider(), Thread.currentThread().getName());

        try {
            while (running) {
                cleanupTransportWorker();
                if (!running) {
                    break;
                }

                ChannelSession current = activeSession;
                if (current != null) {
                    if (!leaseValid() || !current.channel().isConnected()) {
                        invalidateIfCurrent(
                                current,
                                new IllegalStateException("Published Shetab session became invalid"),
                                "CONNECTION",
                                leaseValid() ? "SESSION_INVALIDATED" : "LEASE_LOST"
                        );
                        continue;
                    }
                    waitForSignal(1_000L);
                    continue;
                }

                if (!snapshot.recoveryRequired()) {
                    waitForSignal(1_000L);
                    continue;
                }

                waitForRetryWindow();
                if (!running || activeSession != null || !snapshot.recoveryRequired()) {
                    continue;
                }
                attemptConnectionWorker();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            if (running) {
                unexpectedTermination = true;
                terminalFailure = exception;
            }
        } catch (Throwable failure) {
            unexpectedTermination = true;
            terminalFailure = failure;
        } finally {
            SessionInvalidation invalidation = null;
            synchronized (stateLock) {
                running = false;
                ChannelSession current = activeSession;
                if (current != null) {
                    invalidation = invalidateLocked(
                            current,
                            terminalFailure,
                            "LIFECYCLE",
                            unexpectedTermination ? "WORKER_TERMINATED" : "CLIENT_STOPPING"
                    );
                }
                releaseLeaseRequired = true;
            }
            notifyInvalidation(invalidation);
            cleanupTransportWorker();

            synchronized (stateLock) {
                boolean failed = unexpectedTermination;
                transitionLocked(
                        failed ? "WORKER_TERMINATED" : "CLIENT_STOPPING",
                        "LIFECYCLE",
                        terminalFailure,
                        builder -> {
                            builder.connectionWorkerAlive = false;
                            builder.recoveryInProgress = false;
                            builder.recoveryRequired = failed;
                            builder.connectionState = failed ? ConnectionState.FAILED : ConnectionState.STOPPED;
                            builder.endpoint = null;
                            builder.leaseState = leasingRequired() ? LeaseState.UNKNOWN : LeaseState.NOT_REQUIRED;
                            builder.nextRetryDelayMs = null;
                        }
                );
                retryNotBeforeNanos = 0L;
                connectionWorker = null;
                stateLock.notifyAll();
            }

            if (unexpectedTermination) {
                log.error("event=SHETAB_CONNECTION_WORKER_TERMINATED provider={} reasonCode=WORKER_TERMINATED "
                                + "phase=LIFECYCLE threadName={} causeType={} causeMessage={}",
                        config.provider(), Thread.currentThread().getName(),
                        typeName(terminalFailure), safeExceptionMessage(terminalFailure));
            } else {
                log.info("event=SHETAB_CONNECTION_WORKER_STOPPED provider={} reasonCode=CLIENT_STOPPING threadName={}",
                        config.provider(), Thread.currentThread().getName());
            }
        }
    }

    private void attemptConnectionWorker() throws InterruptedException {
        boolean reconnect;
        int attempt;
        int reconnectAttempt;
        synchronized (stateLock) {
            reconnect = generationSequence > 0 || snapshot.lastDisconnectedAt() != null;
            retryNotBeforeNanos = 0L;
            transitionLocked(reconnect ? "SESSION_INVALIDATED" : "APPLICATION_READY", "CONNECT", null, builder -> {
                builder.connectionState = reconnect ? ConnectionState.RECONNECTING : ConnectionState.CONNECTING;
                builder.recoveryInProgress = true;
                builder.recoveryRequired = true;
                builder.connectionAttempt++;
                if (reconnect) {
                    builder.reconnectAttempt++;
                }
                builder.nextRetryDelayMs = null;
            });
            attempt = snapshot.connectionAttempt();
            reconnectAttempt = snapshot.reconnectAttempt();
        }

        log.info("event=SHETAB_CONNECTION_ATTEMPT_STARTED provider={} reasonCode={} phase=CONNECT "
                        + "endpoint={} attempt={} reconnectAttempt={} sameEndpointFailureCount={} threadName={}",
                config.provider(), reconnect ? "SESSION_INVALIDATED" : "APPLICATION_READY",
                snapshot.endpoint(), attempt, reconnectAttempt, snapshot.sameEndpointFailureCount(),
                Thread.currentThread().getName());

        ShetabEndpointLease lease;
        try {
            lease = ensureLeaseWorker();
        } catch (RuntimeException failure) {
            recordConnectionFailure(failure, "LEASE_ACQUIRE_FAILED", "LEASE");
            cleanupTransportWorker();
            return;
        }

        ISOChannel channel = null;
        try {
            if (!running) {
                return;
            }
            if (!lease.isValid()) {
                throw new IllegalStateException("Shetab endpoint lease ownership is not valid");
            }

            channel = createChannel(lease);
            channel.connect();
            if (!channel.isConnected()) {
                throw new IOException("Shetab channel did not become connected");
            }

            ChannelSession published;
            synchronized (stateLock) {
                if (!running || activeSession != null || !lease.isValid()) {
                    published = null;
                } else {
                    published = new ChannelSession(++generationSequence, channel, lease.endpoint());
                    activeSession = published;
                    transitionLocked("TCP_CONNECT_SUCCEEDED", "CONNECT", null, builder -> {
                        builder.connectionState = ConnectionState.CONNECTED;
                        builder.verified = false;
                        builder.recoveryRequired = false;
                        builder.recoveryInProgress = false;
                        builder.endpoint = published.endpoint();
                        builder.generation = published.generation();
                        builder.leaseState = lease.leasingRequired() ? LeaseState.VALID : LeaseState.NOT_REQUIRED;
                        builder.lastConnectedAt = Instant.now();
                        builder.nextRetryDelayMs = null;
                    });
                    retryNotBeforeNanos = 0L;
                    stateLock.notifyAll();
                }
            }

            if (published == null) {
                disconnectQuietlyWorker(channel);
                return;
            }

            channel = null;
            repeatedFailureSignature = null;
            repeatedFailureCount = 0;
            log.info("event=SHETAB_CONNECTION_ESTABLISHED provider={} reasonCode=TCP_CONNECT_SUCCEEDED "
                            + "phase=CONNECT endpoint={} generation={} attempt={} reconnectAttempt={} "
                            + "sameEndpointFailureCount={} recoveryRequired=false threadName={}",
                    config.provider(), published.endpoint(), published.generation(), attempt, reconnectAttempt,
                    snapshot.sameEndpointFailureCount(), Thread.currentThread().getName());
        } catch (InterruptedException exception) {
            disconnectQuietlyWorker(channel);
            throw exception;
        } catch (Exception failure) {
            disconnectQuietlyWorker(channel);
            recordConnectionFailure(failure, "TCP_CONNECT_FAILED", "CONNECT");
            cleanupTransportWorker();
        }
    }

    private ShetabEndpointLease ensureLeaseWorker() {
        ShetabEndpointLease current;
        synchronized (stateLock) {
            current = leasedEndpoint;
        }
        if (!emptyLease(current) && current.isValid()) {
            return current;
        }
        if (!emptyLease(current)) {
            synchronized (stateLock) {
                if (leasedEndpoint == current) {
                    leasedEndpoint = ShetabEndpointLease.none();
                }
            }
            closeLeaseQuietlyWorker(current);
        }

        ShetabEndpointLease acquired = endpointLeaseManager.acquire(config);
        if (emptyLease(acquired) || !acquired.isValid()) {
            closeLeaseQuietlyWorker(acquired);
            throw new IllegalStateException("Shetab endpoint lease is unavailable provider=" + config.provider());
        }
        acquired.onInvalidated(() -> onLeaseInvalidated(acquired));

        ShetabEndpointLease selected;
        synchronized (stateLock) {
            if (!running || !emptyLease(leasedEndpoint)) {
                selected = leasedEndpoint;
            } else {
                leasedEndpoint = acquired;
                transitionLocked("LEASE_ACQUIRED", "LEASE", null, builder -> {
                    builder.endpoint = acquired.endpoint();
                    builder.leaseState = acquired.leasingRequired() ? LeaseState.VALID : LeaseState.NOT_REQUIRED;
                });
                selected = acquired;
            }
        }
        if (selected != acquired) {
            closeLeaseQuietlyWorker(acquired);
            return selected;
        }
        if (!selected.isValid()) {
            onLeaseInvalidated(selected);
            throw new IllegalStateException("Shetab endpoint lease became invalid during acquisition");
        }
        return selected;
    }

    private void onLeaseInvalidated(ShetabEndpointLease invalidLease) {
        SessionInvalidation invalidation = null;
        Long retryMs = null;
        synchronized (stateLock) {
            if (leasedEndpoint != invalidLease) {
                return;
            }
            releaseLeaseRequired = true;
            ChannelSession current = activeSession;
            if (current != null) {
                invalidation = invalidateLocked(
                        current,
                        new IllegalStateException("Shetab endpoint lease ownership was lost"),
                        "LEASE",
                        "LEASE_LOST"
                );
            } else {
                if (running) {
                    retryMs = scheduleRetryLocked();
                }
                Long scheduledDelayMs = retryMs;
                transitionLocked("LEASE_LOST", "LEASE", null, builder -> {
                    builder.connectionState = ConnectionState.FAILED;
                    builder.leaseState = LeaseState.LOST;
                    builder.recoveryRequired = true;
                    builder.recoveryInProgress = false;
                    builder.nextRetryDelayMs = scheduledDelayMs;
                });
                stateLock.notifyAll();
            }
        }
        notifyInvalidation(invalidation);
        if (retryMs != null) {
            logRecoveryScheduled("LEASE_LOST", retryMs);
        }
        log.warn("event=SHETAB_ENDPOINT_LEASE_LOST provider={} reasonCode=LEASE_LOST phase=LEASE "
                        + "endpoint={} generation={} recoveryRequired=true threadName={}",
                config.provider(), invalidLease.endpoint(), snapshot.generation(), Thread.currentThread().getName());
    }

    private SessionInvalidation invalidateLocked(
            ChannelSession failedSession,
            Throwable failure,
            String phase,
            String reasonCode
    ) {
        activeSession = null;
        sessionsToClose.add(failedSession);
        int nextFailureCount = snapshot.sameEndpointFailureCount() + 1;
        Long retryMs = running ? scheduleRetryLocked() : null;
        if ("RECEIVE_IDLE_TIMEOUT".equals(reasonCode)
                || "LEASE_LOST".equals(reasonCode)
                || nextFailureCount >= maxSameEndpointReconnectAttempts()) {
            releaseLeaseRequired = true;
        }
        transitionLocked(reasonCode, phase, failure, builder -> {
            builder.connectionState = ConnectionState.RECONNECTING;
            builder.verified = false;
            builder.recoveryRequired = true;
            builder.recoveryInProgress = false;
            builder.lastDisconnectedAt = Instant.now();
            builder.sameEndpointFailureCount = nextFailureCount;
            builder.nextRetryDelayMs = retryMs;
            if ("LEASE_LOST".equals(reasonCode)) {
                builder.leaseState = LeaseState.LOST;
            }
        });
        stateLock.notifyAll();

        log.warn("event=SHETAB_CONNECTION_INVALIDATED provider={} reasonCode={} phase={} endpoint={} "
                        + "generation={} sameEndpointFailureCount={} recoveryRequired=true threadName={} "
                        + "causeType={} causeMessage={}",
                config.provider(), reasonCode, phase, failedSession.endpoint(), failedSession.generation(),
                nextFailureCount, Thread.currentThread().getName(), typeName(failure), safeExceptionMessage(failure));
        if (retryMs != null) {
            logRecoveryScheduled(reasonCode, retryMs);
        }
        return new SessionInvalidation(failedSession.generation(), reasonCode, phase, failure);
    }

    private void recordConnectionFailure(Throwable failure, String reasonCode, String phase) {
        long retryMs;
        synchronized (stateLock) {
            int nextFailureCount = snapshot.sameEndpointFailureCount() + 1;
            retryMs = scheduleRetryLocked();
            if (nextFailureCount >= maxSameEndpointReconnectAttempts()) {
                releaseLeaseRequired = true;
            }
            transitionLocked(reasonCode, phase, failure, builder -> {
                builder.connectionState = ConnectionState.FAILED;
                builder.recoveryRequired = true;
                builder.recoveryInProgress = false;
                builder.sameEndpointFailureCount = nextFailureCount;
                builder.nextRetryDelayMs = retryMs;
            });
        }
        logAttemptFailure(failure, reasonCode, phase);
        logRecoveryScheduled(reasonCode, retryMs);
    }

    private void waitForRetryWindow() throws InterruptedException {
        synchronized (stateLock) {
            while (running && activeSession == null && snapshot.recoveryRequired()) {
                long remaining = retryNotBeforeNanos - System.nanoTime();
                if (remaining <= 0L) {
                    break;
                }
                TimeUnit.NANOSECONDS.timedWait(stateLock, remaining);
            }
            if (running && retryNotBeforeNanos != 0L && retryNotBeforeNanos - System.nanoTime() <= 0L) {
                retryNotBeforeNanos = 0L;
                transitionLocked("RETRY_DELAY_ELAPSED", "CONNECT", null,
                        builder -> builder.nextRetryDelayMs = null);
            }
        }
    }

    private long scheduleRetryLocked() {
        long retryMs = Math.max(1L, config.reconnectDelayMs());
        retryNotBeforeNanos = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(retryMs);
        return retryMs;
    }

    private void logRecoveryScheduled(String reasonCode, long retryMs) {
        log.info("event=SHETAB_CONNECTION_RECOVERY_SCHEDULED provider={} reasonCode={} "
                        + "phase=CONNECT reconnectAttempt={} nextRetryMs={} recoveryRequired=true threadName={}",
                config.provider(), reasonCode, snapshot.reconnectAttempt(), retryMs,
                Thread.currentThread().getName());
    }

    private void waitForSignal(long maximumWaitMs) throws InterruptedException {
        synchronized (stateLock) {
            if (running) {
                stateLock.wait(maximumWaitMs);
            }
        }
    }

    private void cleanupTransportWorker() {
        List<ChannelSession> retired = new ArrayList<>();
        ShetabEndpointLease leaseToClose = null;
        synchronized (stateLock) {
            while (!sessionsToClose.isEmpty()) {
                retired.add(sessionsToClose.removeFirst());
            }
            if (releaseLeaseRequired) {
                releaseLeaseRequired = false;
                if (!emptyLease(leasedEndpoint)) {
                    leaseToClose = leasedEndpoint;
                    leasedEndpoint = ShetabEndpointLease.none();
                    transitionLocked("LEASE_RELEASED", "LEASE", null, builder -> {
                        builder.endpoint = null;
                        builder.leaseState = leasingRequired() ? LeaseState.UNKNOWN : LeaseState.NOT_REQUIRED;
                        builder.sameEndpointFailureCount = 0;
                    });
                }
            }
        }
        retired.forEach(session -> disconnectQuietlyWorker(session.channel()));
        closeLeaseQuietlyWorker(leaseToClose);
    }

    private ISOChannel createChannel(ShetabEndpointLease endpointLease) throws Exception {
        String remoteHost = endpointLease.remoteHost();
        int remotePort = endpointLease.remotePort();
        if (remoteHost == null || remoteHost.isBlank() || remotePort <= 0) {
            throw new IllegalStateException("Invalid leased endpoint for provider "
                    + config.provider() + ": " + endpointLease.endpoint());
        }

        ASCIIChannel channel = new ASCIIChannel(remoteHost, remotePort, packagerFactory.create(config));
        Properties channelConfig = new Properties();
        channelConfig.put("host", remoteHost);
        channelConfig.put("port", String.valueOf(remotePort));
        channelConfig.put("timeout", String.valueOf(config.socketTimeoutMs()));
        channelConfig.put("connect-timeout", String.valueOf(config.connectTimeoutMs()));
        channelConfig.put("keep-alive", Boolean.toString(config.keepAlive()));
        channelConfig.put("length-digits", String.valueOf(SHETAB_LENGTH_DIGITS));
        channel.setConfiguration(new SimpleConfiguration(channelConfig));
        return channel;
    }

    private void disconnectQuietlyWorker(ISOChannel channel) {
        if (channel == null) {
            return;
        }
        try {
            channel.disconnect();
        } catch (Exception exception) {
            log.debug("Could not close Shetab channel provider={} causeType={} causeMessage={}",
                    config.provider(), typeName(exception), safeExceptionMessage(exception));
        }
    }

    private void closeLeaseQuietlyWorker(ShetabEndpointLease lease) {
        if (emptyLease(lease)) {
            return;
        }
        try {
            lease.close();
        } catch (Exception exception) {
            log.warn("Could not release Shetab endpoint lease provider={} endpoint={} causeType={} causeMessage={}",
                    config.provider(), lease.endpoint(), typeName(exception), safeExceptionMessage(exception));
        }
    }

    private boolean leaseValid() {
        ShetabEndpointLease current;
        synchronized (stateLock) {
            current = leasedEndpoint;
        }
        return !emptyLease(current) && current.isValid();
    }

    private boolean emptyLease(ShetabEndpointLease lease) {
        return lease == null || lease.endpoint() == null || lease.endpoint().isBlank();
    }

    private boolean leasingRequired() {
        return config.endpointLease() != null && config.endpointLease().enabled();
    }

    private int maxSameEndpointReconnectAttempts() {
        return Math.max(1, config.sameEndpointReconnectAttempts());
    }

    private boolean isCurrentSessionLocked(ChannelSession candidate) {
        return candidate != null
                && activeSession == candidate;
    }

    private void notifyInvalidation(SessionInvalidation invalidation) {
        if (invalidation == null) {
            return;
        }
        try {
            invalidationListener.accept(invalidation);
        } catch (RuntimeException listenerFailure) {
            log.warn("Shetab invalidation listener failed provider={} generation={} causeType={} causeMessage={}",
                    config.provider(), invalidation.generation(), typeName(listenerFailure),
                    safeExceptionMessage(listenerFailure));
        }
    }

    private void transitionLocked(
            String reasonCode,
            String phase,
            Throwable failure,
            Consumer<ShetabConnectionSnapshot.Builder> mutation
    ) {
        ShetabConnectionSnapshot previous = snapshot;
        ShetabConnectionSnapshot.Builder builder = ShetabConnectionSnapshot.builder(previous);
        mutation.accept(builder);
        if (failure != null) {
            builder.lastFailureAt = Instant.now();
            builder.lastFailurePhase = phase;
            builder.lastFailureCode = reasonCode;
            builder.lastFailureType = typeName(failure);
            builder.lastFailureMessage = safeExceptionMessage(failure);
        }
        if (builder.connectionState != previous.connectionState()) {
            builder.lastStateChangedAt = Instant.now();
        }
        ShetabConnectionSnapshot next = builder.build();
        snapshot = next;

        if (next.connectionState() != previous.connectionState()) {
            String template = "event=SHETAB_CONNECTION_STATE_CHANGED provider={} fromState={} toState={} "
                    + "reasonCode={} phase={} endpoint={} generation={} attempt={} reconnectAttempt={} "
                    + "sameEndpointFailureCount={} recoveryRequired={} recoveryInProgress={} nextRetryMs={} "
                    + "threadName={} causeType={} causeMessage={} rootCauseType={} rootCauseMessage={}";
            Object[] arguments = {
                    config.provider(), previous.connectionState(), next.connectionState(), reasonCode, phase,
                    next.endpoint(), next.generation(), next.connectionAttempt(), next.reconnectAttempt(),
                    next.sameEndpointFailureCount(), next.recoveryRequired(), next.recoveryInProgress(),
                    next.nextRetryDelayMs(), Thread.currentThread().getName(), typeName(failure),
                    safeExceptionMessage(failure), rootTypeName(failure), safeRootMessage(failure)
            };
            if (next.connectionState() == ConnectionState.FAILED) {
                log.warn(template, arguments);
            } else {
                log.info(template, arguments);
            }
        }
    }

    private void logAttemptFailure(Throwable failure, String reasonCode, String phase) {
        String signature = typeName(failure) + '|' + safeExceptionMessage(failure) + '|' + phase;
        if (signature.equals(repeatedFailureSignature)) {
            repeatedFailureCount++;
        } else {
            repeatedFailureSignature = signature;
            repeatedFailureCount = 1;
        }

        String template = "event=SHETAB_CONNECTION_ATTEMPT_FAILED provider={} reasonCode={} phase={} "
                + "endpoint={} generation={} attempt={} reconnectAttempt={} sameEndpointFailureCount={} "
                + "recoveryRequired=true nextRetryMs={} threadName={} causeType={} causeMessage={} "
                + "rootCauseType={} rootCauseMessage={} identicalFailureCount={}";
        Object[] arguments = {
                config.provider(), reasonCode, phase, snapshot.endpoint(), snapshot.generation(),
                snapshot.connectionAttempt(), snapshot.reconnectAttempt(), snapshot.sameEndpointFailureCount(),
                Math.max(1L, config.reconnectDelayMs()), Thread.currentThread().getName(), typeName(failure),
                safeExceptionMessage(failure), rootTypeName(failure), safeRootMessage(failure), repeatedFailureCount
        };
        if (repeatedFailureCount == 1 || repeatedFailureCount % REPEATED_FAILURE_SUMMARY_INTERVAL == 0) {
            log.warn(template, arguments);
        } else {
            log.debug(template, arguments);
        }
    }

    private String typeName(Throwable failure) {
        return failure == null ? null : failure.getClass().getName();
    }

    private String rootTypeName(Throwable failure) {
        Throwable root = rootCause(failure);
        return root == null ? null : root.getClass().getName();
    }

    private String safeRootMessage(Throwable failure) {
        return safeExceptionMessage(rootCause(failure));
    }

    private Throwable rootCause(Throwable failure) {
        Throwable current = failure;
        while (current != null && current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    private String safeExceptionMessage(Throwable failure) {
        if (failure == null) {
            return null;
        }
        String message = failure.getMessage();
        String safe = message == null || message.isBlank() ? failure.getClass().getSimpleName() : message;
        safe = safe.replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
        return safe.length() <= 300 ? safe : safe.substring(0, 300) + "...[truncated]";
    }

    record InvalidationResult(boolean invalidated, long generation) {
        static InvalidationResult invalidated(long generation) {
            return new InvalidationResult(true, generation);
        }

        static InvalidationResult notInvalidated(long generation) {
            return new InvalidationResult(false, generation);
        }
    }

    record SessionInvalidation(
            long generation,
            String reasonCode,
            String phase,
            Throwable failure
    ) {
    }
}

record ChannelSession(
        long generation,
        ISOChannel channel,
        String endpoint
) {
}
