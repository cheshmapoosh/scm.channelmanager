package ir.daneshrefah.scm.provider.shetab.tcp;

import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import ir.daneshrefah.scm.provider.shetab.iso.ShetabPackagerFactory;
import ir.daneshrefah.scm.provider.shetab.iso.log.SafeIsoLogFormatter;
import ir.daneshrefah.scm.provider.shetab.lease.ShetabEndpointLeaseManager;
import ir.daneshrefah.scm.provider.shetab.metrics.ShetabProviderMetrics;
import ir.daneshrefah.scm.provider.shetab.trace.ShetabProviderTraceLifecycle;
import lombok.extern.slf4j.Slf4j;
import org.jpos.iso.ISOMsg;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ShetabIsoChannelClient {
    private final ShetabResolvedConfig config;
    private final ShetabProviderMetrics.CounterSet metrics;
    private final ArrayBlockingQueue<PendingRequest> sendQueue;
    private final ShetabChannelSessionManager sessionManager;
    private final ShetabResponseRegistry responseRegistry;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Object sendLock = new Object();

    private Thread senderThread;
    private Thread receiverThread;

    public ShetabIsoChannelClient(
            ShetabResolvedConfig config,
            ShetabPackagerFactory packagerFactory,
            ShetabEndpointLeaseManager endpointLeaseManager,
            ShetabProviderMetrics metrics
    ) {
        this.config = Objects.requireNonNull(config, "config");
        Objects.requireNonNull(packagerFactory, "packagerFactory");
        Objects.requireNonNull(endpointLeaseManager, "endpointLeaseManager");
        this.metrics = Objects.requireNonNull(metrics, "metrics").provider(config.provider());
        this.sendQueue = new ArrayBlockingQueue<>(Math.max(1, config.queueCapacity()));
        this.sessionManager = new ShetabChannelSessionManager(config, packagerFactory, endpointLeaseManager);
        this.sessionManager.setInvalidationListener(this::handleSessionInvalidation);
        this.responseRegistry = new ShetabResponseRegistry(config.provider());
    }

    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }

        senderThread = daemonThread("scm-shetab-sender-" + config.provider(), this::senderLoop);
        receiverThread = daemonThread("scm-shetab-receiver-" + config.provider(), this::receiverLoop);
        sessionManager.startWorker();
        senderThread.start();
        receiverThread.start();
    }

    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        sessionManager.stopWorker();
        interruptQuietly(senderThread);
        interruptQuietly(receiverThread);

        IllegalStateException stopped = new IllegalStateException(
                "reasonCode=SHETAB_CONNECTION_UNAVAILABLE Shetab client stopped provider=" + config.provider());
        failAllPending(stopped);
        drainSendQueue(stopped);
        joinQuietly(senderThread);
        joinQuietly(receiverThread);
    }

    public ShetabTransportResponse request(
            ISOMsg message,
            int timeoutMs,
            ShetabProviderTraceLifecycle traceLifecycle
    ) {
        Objects.requireNonNull(traceLifecycle, "traceLifecycle");
        metrics.submitted();

        if (!running.get()) {
            metrics.failed();
            throw connectionUnavailable("client is not active");
        }

        ChannelSession admittedSession = sessionManager.admitCurrentSession();
        if (admittedSession == null) {
            sessionManager.signalRecovery("SESSION_INVALIDATED");
            metrics.failed();
            throw connectionUnavailable("no published valid session");
        }

        ShetabCorrelationKey correlationKey = ShetabCorrelationKey.from(message);
        Deadline deadline = Deadline.afterMillis(Math.max(1, timeoutMs));
        ResponseTracker tracker = null;
        boolean queued = false;

        try {
            tracker = responseRegistry.register(correlationKey, deadline, traceLifecycle);
            tracker.markAdmitted(admittedSession.generation());
            if (!sessionManager.isActive(admittedSession)) {
                throw connectionUnavailable("session changed during request admission");
            }

            PendingRequest pendingRequest = new PendingRequest(tracker, message, admittedSession);
            long queueWaitMs = Math.min(
                    Math.max(1L, config.sendTimeoutMs()),
                    Math.max(1L, deadline.remainingMillisCeiling())
            );
            queued = sendQueue.offer(pendingRequest, queueWaitMs, TimeUnit.MILLISECONDS);
            if (!queued) {
                if (deadline.isExpired()) {
                    throw deadlineExceeded(tracker, message, null);
                }
                RejectedExecutionException error = new RejectedExecutionException(
                        "Shetab send queue is full provider=" + config.provider());
                responseRegistry.failIfActive(tracker, error);
                metrics.queueRejected();
                throw error;
            }

            if (!tracker.future().isDone()) {
                verifyPending(tracker, "after queue admission");
            }
            return awaitResponse(message, tracker);
        } catch (ShetabRequestDeadlineExceededException exception) {
            handleRequestDeadline(tracker, message, exception);
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            IllegalStateException interrupted = new IllegalStateException(
                    "Interrupted while waiting Shetab response provider=" + config.provider()
                            + " key=" + displayKey(tracker, correlationKey),
                    exception);
            if (tracker != null) {
                responseRegistry.failIfActive(tracker, interrupted);
            }
            throw interrupted;
        } catch (TimeoutException exception) {
            ShetabRequestDeadlineExceededException timeout = deadlineExceeded(tracker, message, exception);
            handleRequestDeadline(tracker, message, timeout);
            throw timeout;
        } catch (ExecutionException exception) {
            return handleRequestExecutionFailure(tracker, message, exception);
        } catch (RuntimeException exception) {
            if (tracker != null) {
                responseRegistry.failIfActive(tracker, exception);
            }
            if (!queued) {
                metrics.failed();
            }
            throw exception;
        }
    }

    public ShetabConnectionSnapshot snapshot() {
        return sessionManager.snapshot();
    }

    private void senderLoop() {
        while (running.get()) {
            PendingRequest pending = null;
            try {
                pending = sendQueue.poll(100L, TimeUnit.MILLISECONDS);
                if (pending != null) {
                    sendPending(pending);
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            } catch (ShetabRequestNoLongerPendingException | ShetabRequestDeadlineExceededException exception) {
                if (pending != null) {
                    responseRegistry.failIfActive(pending.tracker(), exception);
                }
                log.debug("Shetab sender ignored inactive request provider={} reason={}",
                        config.provider(), safeExceptionMessage(exception));
            } catch (Exception exception) {
                if (!running.get()) {
                    return;
                }
                if (pending != null) {
                    responseRegistry.failIfActive(pending.tracker(), exception);
                }
                log.warn("Shetab sender failed provider={} pendingKey={} causeType={} causeMessage={}",
                        config.provider(), pending != null ? pending.key() : null,
                        exception.getClass().getName(), safeExceptionMessage(exception));
            }
        }
    }

    private void receiverLoop() {
        while (running.get()) {
            ChannelSession session = null;
            try {
                session = sessionManager.waitForActiveSession(running::get);
                if (session == null) {
                    continue;
                }

                ISOMsg response = session.channel().receive();
                ShetabCorrelationKey responseKey = ShetabCorrelationKey.from(response);
                ShetabResponseRegistry.MatchResult match = responseRegistry.match(responseKey, session.generation());

                if (match.ambiguous()) {
                    logWireDebug("received", response);
                    metrics.failed();
                    log.warn("Shetab receive ambiguous provider={} keys={} mti={} stan={} rrn={} causeType={}",
                            config.provider(), responseKey.displayKeys(), safeMti(response), safeField(response, 11),
                            safeField(response, 37), match.ambiguity().getClass().getName());
                    continue;
                }
                if (!match.matched()) {
                    logWireDebug("received", response);
                    log.trace("Shetab receive unmatched provider={} keys={} mti={} stan={} rrn={}",
                            config.provider(), responseKey.displayKeys(), safeMti(response), safeField(response, 11),
                            safeField(response, 37));
                    continue;
                }

                ResponseTracker tracker = match.tracker();
                long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - tracker.startedNanos());
                boolean completed = tracker.future().complete(response);
                metrics.received();
                metrics.addLatency(elapsedMs);
                sessionManager.markValidated(session);
                logWireDebug("received", response, elapsedMs);
                log.debug("event=SHETAB_RESPONSE_MATCHED provider={} endpoint={} generation={} "
                                + "mti={} stan={} rrn={} responseCode={} elapsedMs={} completed={}",
                        config.provider(), session.endpoint(), session.generation(), safeMti(response),
                        safeField(response, 11), safeField(response, 37), safeField(response, 39), elapsedMs, completed);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            } catch (SocketTimeoutException exception) {
                String reasonCode = config.socketTimeoutMs() > 0 ? "RECEIVE_IDLE_TIMEOUT" : "RECEIVE_FAILED";
                if (config.socketTimeoutMs() > 0) {
                    log.warn("event=SHETAB_RECEIVE_IDLE_TIMEOUT provider={} reasonCode=RECEIVE_IDLE_TIMEOUT "
                                    + "phase=RECEIVE endpoint={} generation={} socketTimeoutMs={} threadName={}",
                            config.provider(), session != null ? session.endpoint() : null,
                            session != null ? session.generation() : -1L, config.socketTimeoutMs(),
                            Thread.currentThread().getName());
                }
                handleReceiverFailure(session, exception, reasonCode);
            } catch (IOException exception) {
                handleReceiverFailure(session, exception, "RECEIVE_FAILED");
            } catch (Exception exception) {
                handleReceiverFailure(session, exception, "RECEIVE_FAILED");
            }
        }
    }

    private void sendPending(PendingRequest pending) {
        ResponseTracker tracker = pending.tracker();
        verifyPending(tracker, "before send attempt");
        ChannelSession session = pending.session();
        if (!sessionManager.isActive(session)) {
            ShetabConnectionUnavailableException unavailable = connectionUnavailable(
                    "admitted generation is no longer active generation=" + session.generation());
            responseRegistry.failIfActive(tracker, unavailable);
            throw unavailable;
        }
        tracker.startAttempt(session.endpoint());
        sendOnSession(pending, session);
    }

    private void sendOnSession(PendingRequest pending, ChannelSession session) {
        ResponseTracker tracker = pending.tracker();
        verifyPending(tracker, "before send");
        if (!sessionManager.isActive(session)) {
            throw connectionUnavailable("session invalid before send generation=" + session.generation());
        }

        logIsoRequestBeforeSend(pending.msg(), tracker.correlationKey().displayKeys());
        synchronized (sendLock) {
            verifyPending(tracker, "immediately before send");
            if (!sessionManager.isActive(session)) {
                throw connectionUnavailable("session invalid immediately before send generation=" + session.generation());
            }
            boolean sendAdmitted = sessionManager.beginSendIfCurrent(
                    session,
                    () -> responseRegistry.beginSend(tracker, session.generation())
            );
            if (!sendAdmitted) {
                if (!sessionManager.isActive(session)) {
                    throw connectionUnavailable(
                            "session invalid at send admission generation=" + session.generation());
                }
                throw new ShetabRequestNoLongerPendingException(
                        "Shetab request tracker inactive before send provider="
                                + config.provider() + " key=" + tracker.correlationKey().display());
            }

            try {
                session.channel().send(pending.msg());
            } catch (Exception exception) {
                ShetabConnectionLostAfterSendException deliveryFailure = connectionLostAfterSend(
                        session.generation(), "send failed after ISOChannel.send() was entered", exception);
                ShetabChannelSessionManager.InvalidationResult invalidation = sessionManager.invalidateIfCurrent(
                        session, exception, "SEND", "SEND_FAILED");
                if (!invalidation.invalidated()) {
                    responseRegistry.failIfActive(tracker, deliveryFailure);
                }
                log.warn("event=SHETAB_SEND_FAILURE provider={} reasonCode=SEND_FAILED phase=SEND "
                                + "endpoint={} generation={} key={} causeType={} causeMessage={}",
                        config.provider(), session.endpoint(), session.generation(), tracker.correlationKey().display(),
                        exception.getClass().getName(), safeExceptionMessage(exception));
                return;
            }
        }

        responseRegistry.markSent(tracker);
        metrics.sent();
        logWireDebug("sent", pending.msg());
        if (!tracker.future().isDone() && responseRegistry.contains(tracker) && tracker.deadline().isExpired()) {
            throw deadlineExceeded(tracker, pending.msg(), null);
        }
    }

    private ShetabTransportResponse awaitResponse(ISOMsg message, ResponseTracker tracker)
            throws InterruptedException, ExecutionException, TimeoutException {
        long waitMs = tracker.deadline().remainingMillisCeiling();
        if (waitMs <= 0L) {
            throw new TimeoutException("Shetab request deadline expired before response wait");
        }
        ISOMsg response = tracker.future().get(waitMs, TimeUnit.MILLISECONDS);
        if (tracker.deadline().isExpired()) {
            throw deadlineExceeded(tracker, message, null);
        }
        return new ShetabTransportResponse(response, tracker.releaseActiveAttempt());
    }

    private ShetabTransportResponse handleRequestExecutionFailure(
            ResponseTracker tracker,
            ISOMsg message,
            ExecutionException exception
    ) {
        Throwable cause = exception.getCause() != null ? exception.getCause() : exception;
        if (cause instanceof ShetabRequestDeadlineExceededException timeout) {
            recordResponseTimeoutIfCurrent(tracker, timeout);
            metrics.timedOut();
            throw timeout;
        }
        metrics.failed();
        if (cause instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        throw new IllegalStateException(
                "Shetab request failed provider=" + config.provider()
                        + " key=" + displayKey(tracker, ShetabCorrelationKey.from(message)),
                cause);
    }

    private void handleRequestDeadline(
            ResponseTracker tracker,
            ISOMsg message,
            ShetabRequestDeadlineExceededException failure
    ) {
        if (tracker != null) {
            ShetabResponseRegistry.TimeoutClaim claim = responseRegistry.timeout(tracker, failure);
            if (claim.deliveryStarted()) {
                recordResponseTimeoutIfCurrent(tracker, failure);
            }
        }
        metrics.timedOut();
        log.warn("Shetab request timeout provider={} reasonCode=SHETAB_RESPONSE_TIMEOUT key={} "
                        + "mti={} stan={} rrn={} deliveryPhase={}",
                config.provider(), displayKey(tracker, ShetabCorrelationKey.from(message)), safeMti(message),
                safeField(message, 11), safeField(message, 37),
                tracker != null ? tracker.deliveryPhase() : null);
    }

    private void recordResponseTimeoutIfCurrent(ResponseTracker tracker, Throwable failure) {
        if (tracker == null
                || !tracker.deliveryStarted()
                || tracker.connectionGeneration() < 0L) {
            return;
        }
        sessionManager.recordResponseTimeout(tracker.connectionGeneration(), failure);
    }

    private void handleReceiverFailure(ChannelSession session, Exception failure, String reasonCode) {
        if (!running.get() || session == null) {
            return;
        }
        ShetabChannelSessionManager.InvalidationResult invalidation = sessionManager.invalidateIfCurrent(
                session, failure, "RECEIVE", reasonCode);
        if (!invalidation.invalidated()) {
            log.debug("event=SHETAB_STALE_RECEIVER_FAILURE_IGNORED provider={} endpoint={} generation={} "
                            + "reasonCode={} causeType={} causeMessage={}",
                    config.provider(), session.endpoint(), session.generation(), reasonCode,
                    failure.getClass().getName(), safeExceptionMessage(failure));
        }
    }

    private void handleSessionInvalidation(ShetabChannelSessionManager.SessionInvalidation invalidation) {
        List<ResponseTracker> trackers = responseRegistry.removeByGeneration(invalidation.generation());
        for (ResponseTracker tracker : trackers) {
            RuntimeException failure = !tracker.deliveryStarted()
                    ? connectionUnavailable("queued request invalidated before delivery generation="
                    + invalidation.generation())
                    : connectionLostAfterSend(
                    invalidation.generation(),
                    "connection invalidated reasonCode=" + invalidation.reasonCode(),
                    invalidation.failure());
            responseRegistry.completeFailure(tracker, failure);
        }
    }

    private void verifyPending(ResponseTracker tracker, String step) {
        if (!running.get()) {
            throw new ShetabRequestNoLongerPendingException(
                    "Shetab client stopped provider=" + config.provider() + " step=" + step);
        }
        if (tracker.future().isDone() || !responseRegistry.contains(tracker)) {
            throw new ShetabRequestNoLongerPendingException(
                    "Shetab request no longer pending provider=" + config.provider()
                            + " key=" + tracker.correlationKey().display() + " step=" + step);
        }
        if (tracker.deadline().isExpired()) {
            throw deadlineExceeded(tracker, null, null);
        }
    }

    private ShetabRequestDeadlineExceededException deadlineExceeded(
            ResponseTracker tracker,
            ISOMsg message,
            Throwable cause
    ) {
        String text = "reasonCode=SHETAB_RESPONSE_TIMEOUT Shetab response deadline exceeded provider="
                + config.provider() + " key=" + displayKey(tracker, ShetabCorrelationKey.from(message));
        return cause == null
                ? new ShetabRequestDeadlineExceededException(text)
                : new ShetabRequestDeadlineExceededException(text, cause);
    }

    private ShetabConnectionUnavailableException connectionUnavailable(String detail) {
        return new ShetabConnectionUnavailableException(
                "reasonCode=SHETAB_CONNECTION_UNAVAILABLE provider=" + config.provider() + " detail=" + detail);
    }

    private ShetabConnectionLostAfterSendException connectionLostAfterSend(
            long generation,
            String detail,
            Throwable cause
    ) {
        return new ShetabConnectionLostAfterSendException(
                "reasonCode=SHETAB_CONNECTION_LOST_AFTER_SEND provider=" + config.provider()
                        + " generation=" + generation + " detail=" + detail,
                cause);
    }

    private void failAllPending(Throwable failure) {
        for (ResponseTracker tracker : responseRegistry.removeAll()) {
            responseRegistry.completeFailure(tracker, failure);
        }
    }

    private void drainSendQueue(Throwable failure) {
        PendingRequest pending;
        while ((pending = sendQueue.poll()) != null) {
            responseRegistry.failIfActive(pending.tracker(), failure);
        }
    }

    private String displayKey(ResponseTracker tracker, ShetabCorrelationKey fallback) {
        return tracker != null ? tracker.correlationKey().display() : fallback == null ? "" : fallback.display();
    }

    private String safeField(ISOMsg message, int field) {
        try {
            return message != null ? message.getString(field) : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private String safeMti(ISOMsg message) {
        try {
            return message != null && message.hasMTI() ? message.getMTI() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private Thread daemonThread(String name, Runnable runnable) {
        Thread thread = new Thread(runnable, name);
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler((failedThread, failure) ->
                log.error("Uncaught exception in Shetab thread provider={} thread={} causeType={} causeMessage={}",
                        config.provider(), failedThread.getName(), failure.getClass().getName(),
                        safeExceptionMessage(failure)));
        return thread;
    }

    private void interruptQuietly(Thread thread) {
        if (thread != null) {
            thread.interrupt();
        }
    }

    private void joinQuietly(Thread thread) {
        if (thread == null || thread == Thread.currentThread()) {
            return;
        }
        try {
            thread.join(Math.max(1_000L, config.connectTimeoutMs() + 1_000L));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private void logWireDebug(String direction, ISOMsg message) {
        logWireDebug(direction, message, -1L);
    }

    private void logWireDebug(String direction, ISOMsg message, long elapsedMs) {
        if (log.isDebugEnabled()) {
            log.debug("Shetab {} provider={} message={}", direction, config.provider(),
                    SafeIsoLogFormatter.format(SafeIsoLogFormatter.sanitize(message), elapsedMs));
        }
    }

    private void logIsoRequestBeforeSend(ISOMsg message, List<String> requestKeys) {
        if (!log.isTraceEnabled()) {
            return;
        }
        ISOMsg safe = SafeIsoLogFormatter.sanitize(message);
        log.trace("Shetab ISO request before send provider={} keys={} mti={} stan={} rrn={}",
                config.provider(), requestKeys, safeMti(safe), safeField(safe, 11), safeField(safe, 37));
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

    private record PendingRequest(
            ResponseTracker tracker,
            ISOMsg msg,
            ChannelSession session
    ) {
        String key() {
            return tracker.correlationKey().display();
        }
    }
}
