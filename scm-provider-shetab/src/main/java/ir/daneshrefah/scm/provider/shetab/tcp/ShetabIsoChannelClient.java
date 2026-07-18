package ir.daneshrefah.scm.provider.shetab.tcp;

import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import ir.daneshrefah.scm.provider.shetab.iso.ShetabPackagerFactory;
import ir.daneshrefah.scm.provider.shetab.iso.log.SafeIsoLogFormatter;
import ir.daneshrefah.scm.provider.shetab.lease.ShetabEndpointLeaseManager;
import ir.daneshrefah.scm.provider.shetab.metrics.ShetabProviderMetrics;
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
    private final ArrayBlockingQueue<ReconnectCommand> reconnectQueue = new ArrayBlockingQueue<>(1);
    private final ShetabChannelSessionManager sessionManager;
    private final ShetabResponseRegistry responseRegistry;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean reconnectQueued = new AtomicBoolean(false);
    private final Object sendLock = new Object();

    private volatile long lastReceivedAtMillis;
    private volatile long lastSentAtMillis;

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

        int capacity = Math.max(1, config.queueCapacity());
        this.sendQueue = new ArrayBlockingQueue<>(capacity);
        this.sessionManager = new ShetabChannelSessionManager(config, packagerFactory, endpointLeaseManager);
        this.responseRegistry = new ShetabResponseRegistry(config.provider());
    }

    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }

        senderThread = daemonThread("scm-shetab-sender-" + config.provider(), this::senderLoop);
        receiverThread = daemonThread("scm-shetab-receiver-" + config.provider(), this::receiverLoop);

        senderThread.start();
        receiverThread.start();

        log.trace("Started Shetab ISOChannel client provider={} endpointCount={} queueCapacity={}",
                config.provider(),
                config.endpoints() != null ? config.endpoints().size() : 0,
                config.queueCapacity());
    }

    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        log.trace("Stopping Shetab ISOChannel client provider={}", config.provider());

        interruptQuietly(senderThread);
        interruptQuietly(receiverThread);

        IllegalStateException error = new IllegalStateException("Shetab client stopped provider=" + config.provider());
        failAllPending(error);
        drainSendQueue(error);

        joinQuietly(senderThread);
        joinQuietly(receiverThread);

        log.trace("Stopped Shetab ISOChannel client provider={}", config.provider());
    }

    public ISOMsg request(ISOMsg msg, int timeoutMs) {
        metrics.submitted();

        if (!running.get()) {
            throw new IllegalStateException("Shetab client is stopped provider=" + config.provider());
        }

        ShetabCorrelationKey correlationKey = ShetabCorrelationKey.from(msg);
        Deadline deadline = Deadline.afterMillis(Math.max(1, timeoutMs));
        ResponseTracker tracker = null;
        boolean queued = false;

        try {
            tracker = responseRegistry.register(correlationKey, deadline);
            PendingRequest pendingRequest = new PendingRequest(tracker, msg);

            long queueWaitMs = Math.min(
                    Math.max(1L, config.sendTimeoutMs()),
                    Math.max(1L, deadline.remainingMillisCeiling())
            );

            queued = sendQueue.offer(pendingRequest, queueWaitMs, TimeUnit.MILLISECONDS);

            if (!queued) {
                if (deadline.isExpired()) {
                    throw deadlineExceeded(tracker, msg, null);
                }

                RejectedExecutionException error = new RejectedExecutionException(
                        "Shetab send queue is full provider=" + config.provider()
                );
                responseRegistry.failIfActive(tracker, error);
                metrics.queueRejected();
                throw error;
            }

            if (!tracker.future().isDone()) {
                verifyPending(tracker, "after queue wait");
            }

            return awaitResponse(msg, tracker);

        } catch (ShetabRequestDeadlineExceededException e) {
            handleRequestDeadline(tracker, msg, e);
            throw e;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            IllegalStateException error = new IllegalStateException(
                    "Interrupted while waiting Shetab response provider=" + config.provider()
                            + " key=" + displayKey(tracker, correlationKey),
                    e
            );

            if (tracker != null) {
                responseRegistry.failIfActive(tracker, error);
            }

            throw error;

        } catch (TimeoutException e) {
            ShetabRequestDeadlineExceededException error = deadlineExceeded(tracker, msg, e);
            handleRequestDeadline(tracker, msg, error);
            throw error;

        } catch (ExecutionException e) {
            return handleRequestExecutionFailure(tracker, msg, e);

        } catch (RuntimeException e) {
            if (tracker != null) {
                responseRegistry.remove(tracker);
            }

            if (!queued) {
                metrics.failed();
            }

            throw e;
        }
    }

    private void senderLoop() {
        try {
            while (running.get()) {
                SenderCommand command = null;

                try {
                    PendingRequest queuedRequest = sendQueue.poll();
                    if (queuedRequest != null) {
                        command = new SendRequestCommand(queuedRequest);
                    } else {
                        ReconnectCommand queuedReconnect = reconnectQueue.poll();
                        if (queuedReconnect != null) {
                            command = queuedReconnect;
                        } else {
                            queuedRequest = sendQueue.poll(100L, TimeUnit.MILLISECONDS);
                            if (queuedRequest == null) {
                                continue;
                            }
                            command = new SendRequestCommand(queuedRequest);
                        }
                    }

                    if (command instanceof ReconnectCommand(long failedGeneration)) {
                        try {
                            sessionManager.reconnect(failedGeneration, running::get);
                        } finally {
                            reconnectQueued.set(false);
                            long pendingGeneration = sessionManager.reconnectGenerationIfNeeded();
                            if (pendingGeneration >= 0L) {
                                requestReconnect(pendingGeneration);
                            }
                        }
                    } else if (command instanceof SendRequestCommand(PendingRequest pending)) {
                        sendPending(pending);
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;

                } catch (ShetabRequestNoLongerPendingException e) {
                    log.debug("Shetab sender ignored inactive request provider={} reason={}",
                            config.provider(), e.getMessage());

                } catch (ShetabRequestDeadlineExceededException e) {
                    PendingRequest pending = pendingRequest(command);
                    if (pending != null) {
                        responseRegistry.failIfActive(pending.tracker(), e);
                    }

                } catch (Exception e) {
                    if (!running.get()) {
                        return;
                    }

                    PendingRequest pending = pendingRequest(command);
                    log.error("Shetab sender error provider={} pendingKey={} pendingMsg={}",
                            config.provider(),
                            pending != null ? pending.key() : null,
                            pending != null ? SafeIsoLogFormatter.format(pending.msg()) : null,
                            e);

                    if (pending != null) {
                        responseRegistry.failIfActive(pending.tracker(), e);
                    }
                }
            }
        } finally {
            sessionManager.close();
        }
    }

    private void receiverLoop() {
        while (running.get()) {
            ChannelSession session = null;

            try {
                session = sessionManager.waitForActiveSession(() -> running.get());

                if (session == null) {
                    continue;
                }

                ISOMsg response = session.channel().receive();
                lastReceivedAtMillis = System.currentTimeMillis();

                ShetabCorrelationKey responseKey = ShetabCorrelationKey.from(response);
                ShetabResponseRegistry.MatchResult match = responseRegistry.match(responseKey, session.generation());

                if (match.ambiguous()) {
                    logWireDebug("received", response);
                    metrics.failed();
                    log.warn("Shetab receive ambiguous provider={} keys={} mti={} stan={} rrn={}",
                            config.provider(),
                            responseKey.displayKeys(),
                            safeMti(response),
                            safeField(response, 11),
                            safeField(response, 37),
                            match.ambiguity());
                    continue;
                }

                if (!match.matched()) {
                    logWireDebug("received", response);
                    log.trace("Shetab receive unmatched provider={} keys={} mti={} stan={} rrn={}",
                            config.provider(),
                            responseKey.displayKeys(),
                            safeMti(response),
                            safeField(response, 11),
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

                log.trace("Shetab receive matched provider={} keys={} completed={} mti={} stan={} rrn={}",
                        config.provider(),
                        tracker.correlationKey().displayKeys(),
                        completed,
                        safeMti(response),
                        safeField(response, 11),
                        safeField(response, 37));

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;

            } catch (SocketTimeoutException e) {
                log.debug("Shetab receiver socket timeout provider={}", config.provider());
                handleReceiverFailure(session, e);

            } catch (IOException e) {
                handleReceiverFailure(session, e);

            } catch (Exception e) {
                handleReceiverFailure(session, e);
            }
        }
    }

    private void sendPending(PendingRequest pending) throws Exception {
        ResponseTracker tracker = pending.tracker();

        while (running.get()) {
            verifyPending(tracker, "before connection");

            ChannelSession session = sessionManager.ensureConnected(
                    tracker.deadline(),
                    () -> running.get(),
                    step -> verifyPending(tracker, step)
            );

            if (session == null) {
                return;
            }

            verifyPending(tracker, "after connection");

            if (!sessionManager.isActive(session)) {
                continue;
            }

            if (sendOnSession(pending, session)) {
                return;
            }
        }
    }

    private boolean sendOnSession(PendingRequest pending, ChannelSession session) {
        ResponseTracker tracker = pending.tracker();

        verifyPending(tracker, "before send");

        if (!sessionManager.isActive(session)) {
            return false;
        }

        logIsoRequestBeforeSend(pending.msg(), tracker.correlationKey().displayKeys());
        logPackedIsoBeforeSend(pending.msg());

        synchronized (sendLock) {
            verifyPending(tracker, "immediately before send");

            if (!sessionManager.isActive(session)) {
                return false;
            }

            if (!responseRegistry.markSending(tracker, session.generation())) {
                throw new ShetabRequestNoLongerPendingException(
                        "Shetab request tracker inactive before send provider="
                                + config.provider() + " key=" + tracker.correlationKey().display()
                );
            }

            try {
                session.channel().send(pending.msg());
            } catch (Exception e) {
                ShetabAmbiguousProviderDeliveryException deliveryError =
                        new ShetabAmbiguousProviderDeliveryException(
                                "Shetab delivery status is ambiguous after send started provider="
                                        + config.provider()
                                        + " key=" + tracker.correlationKey().display()
                                        + " generation=" + session.generation(),
                                e
                        );

                responseRegistry.failIfActive(tracker, deliveryError);
                ShetabChannelSessionManager.InvalidationResult invalidation =
                        sessionManager.invalidateIfCurrent(session, e);

                if (invalidation.invalidated()) {
                    ShetabConnectionLostAfterSendException connectionError =
                            new ShetabConnectionLostAfterSendException(
                                    "Shetab connection lost after send started provider="
                                            + config.provider()
                                            + " generation=" + session.generation(),
                                    e
                            );
                    failDeliveredByGeneration(invalidation.generation(), connectionError);
                    requestReconnect(invalidation.generation());

                    log.error("Shetab send failed after delivery started provider={} key={} generation={}",
                            config.provider(), tracker.correlationKey().display(), session.generation(), e);
                } else {
                    log.debug("Ignored stale Shetab send failure provider={} generation={} cause={}",
                            config.provider(), session.generation(), rootMessage(e));
                }

                return true;
            }
        }

        responseRegistry.markSent(tracker);
        lastSentAtMillis = System.currentTimeMillis();

        metrics.sent();
        logWireDebug("sent", pending.msg());

        if (!tracker.future().isDone() && responseRegistry.contains(tracker) && tracker.deadline().isExpired()) {
            throw deadlineExceeded(tracker, pending.msg(), null);
        }

        return true;
    }

    private ISOMsg awaitResponse(ISOMsg msg, ResponseTracker tracker)
            throws InterruptedException, ExecutionException, TimeoutException {
        long waitMs = tracker.deadline().remainingMillisCeiling();

        if (waitMs <= 0L) {
            throw new TimeoutException("Shetab request deadline expired before response wait");
        }

        ISOMsg response = tracker.future().get(waitMs, TimeUnit.MILLISECONDS);

        if (tracker.deadline().isExpired()) {
            throw deadlineExceeded(tracker, msg, null);
        }

        return response;
    }

    private ISOMsg handleRequestExecutionFailure(ResponseTracker tracker, ISOMsg msg, ExecutionException e) {
        if (tracker != null) {
            responseRegistry.remove(tracker);
        }

        Throwable cause = e.getCause() != null ? e.getCause() : e;

        if (cause instanceof ShetabRequestDeadlineExceededException deadlineError) {
            recordResponseTimeoutIfCurrent(tracker, deadlineError);
            metrics.timedOut();
            throw deadlineError;
        }

        metrics.failed();

        if (cause instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }

        throw new IllegalStateException(
                "Shetab request failed provider=" + config.provider()
                        + " key=" + displayKey(tracker, ShetabCorrelationKey.from(msg))
                        + ", cause=" + rootMessage(cause),
                cause
        );
    }

    private void handleRequestDeadline(
            ResponseTracker tracker,
            ISOMsg msg,
            ShetabRequestDeadlineExceededException error
    ) {
        if (tracker != null) {
            responseRegistry.remove(tracker);
            boolean completedByDeadline = tracker.future().completeExceptionally(error);

            if (completedByDeadline || tracker.future().isCompletedExceptionally()) {
                recordResponseTimeoutIfCurrent(tracker, error);
            }
        }

        metrics.timedOut();

        log.debug("Shetab request deadline exceeded provider={} key={} mti={} stan={} rrn={}",
                config.provider(),
                displayKey(tracker, ShetabCorrelationKey.from(msg)),
                safeMti(msg),
                safeField(msg, 11),
                safeField(msg, 37));
    }

    private void recordResponseTimeoutIfCurrent(ResponseTracker tracker, Throwable error) {
        if (tracker == null || tracker.deliveryPhase() != DeliveryPhase.SENT) {
            return;
        }

        long generation = tracker.connectionGeneration();

        if (generation != sessionManager.activeGeneration()) {
            return;
        }

        ShetabChannelSessionManager.InvalidationResult invalidation =
                sessionManager.recordResponseTimeout(generation, error);

        if (invalidation.invalidated()) {
            failDeliveredByGeneration(
                    invalidation.generation(),
                    new ShetabConnectionLostAfterSendException(
                            "Shetab connection marked suspect after response timeouts provider="
                                    + config.provider()
                                    + " generation=" + invalidation.generation(),
                            error
                    )
            );
            requestReconnect(invalidation.generation());
        }
    }

    private void handleReceiverFailure(ChannelSession session, Exception e) {
        if (!running.get()) {
            return;
        }

        if (session == null) {
            return;
        }

        ShetabChannelSessionManager.InvalidationResult invalidation =
                sessionManager.invalidateIfCurrent(session, e);

        if (!invalidation.invalidated()) {
            log.debug("Ignored stale Shetab receiver failure provider={} generation={} cause={}",
                    config.provider(), session.generation(), rootMessage(e));
            return;
        }

        log.trace("Shetab receiver socket disconnected provider={} generation={} lastSentAt={} lastReceivedAt={}",
                config.provider(),
                session.generation(),
                lastSentAtMillis,
                lastReceivedAtMillis,
                e);

        ShetabConnectionLostAfterSendException connectionError = new ShetabConnectionLostAfterSendException(
                "Shetab connection lost while receiving provider="
                        + config.provider()
                        + " generation=" + session.generation(),
                e
        );
        failDeliveredByGeneration(invalidation.generation(), connectionError);
        requestReconnect(invalidation.generation());
    }

    private void requestReconnect(long failedGeneration) {
        if (!running.get() || !reconnectQueued.compareAndSet(false, true)) {
            return;
        }
        if (!reconnectQueue.offer(new ReconnectCommand(failedGeneration))) {
            reconnectQueued.set(false);
        }
    }

    private PendingRequest pendingRequest(SenderCommand command) {
        return command instanceof SendRequestCommand send ? send.pending() : null;
    }

    private void failDeliveredByGeneration(long generation, Throwable error) {
        List<ResponseTracker> trackers = responseRegistry.removeDeliveredByGeneration(generation);

        for (ResponseTracker tracker : trackers) {
            tracker.future().completeExceptionally(error);
        }
    }

    private void completeExpiredTrackers() {
        List<ResponseTracker> expiredTrackers = responseRegistry.removeExpired();

        for (ResponseTracker tracker : expiredTrackers) {
            tracker.future().completeExceptionally(deadlineExceeded(tracker, null, null));
        }
    }

    private void verifyPending(ResponseTracker tracker, String step) {
        if (!running.get()) {
            throw new ShetabRequestNoLongerPendingException(
                    "Shetab client stopped provider=" + config.provider() + " step=" + step
            );
        }

        if (tracker.future().isDone() || !responseRegistry.contains(tracker)) {
            throw new ShetabRequestNoLongerPendingException(
                    "Shetab request no longer pending provider="
                            + config.provider()
                            + " key=" + tracker.correlationKey().display()
                            + " step=" + step
            );
        }

        if (tracker.deadline().isExpired()) {
            throw deadlineExceeded(tracker, null, null);
        }
    }

    private ShetabRequestDeadlineExceededException deadlineExceeded(
            ResponseTracker tracker,
            ISOMsg msg,
            Throwable cause
    ) {
        String message = "Shetab request deadline exceeded provider=" + config.provider()
                + " key=" + displayKey(tracker, ShetabCorrelationKey.from(msg))
                + " mti=" + safeMti(msg)
                + " stan=" + safeField(msg, 11)
                + " rrn=" + safeField(msg, 37);

        if (cause == null) {
            return new ShetabRequestDeadlineExceededException(message);
        }

        return new ShetabRequestDeadlineExceededException(message, cause);
    }

    private void failAllPending(Throwable error) {
        List<ResponseTracker> trackers = responseRegistry.removeAll();

        for (ResponseTracker tracker : trackers) {
            tracker.future().completeExceptionally(error);
        }
    }

    private void drainSendQueue(Throwable error) {
        PendingRequest pending;

        while ((pending = sendQueue.poll()) != null) {
            responseRegistry.failIfActive(pending.tracker(), error);
        }
    }

    public boolean isHealthy() {
        return running.get() && sessionManager.isHealthy();
    }

    public Throwable lastConnectionFailure() {
        return sessionManager.lastConnectionFailure();
    }

    public int pendingResponseCount() {
        return responseRegistry.pendingCount();
    }

    public int queuedRequestCount() {
        return sendQueue.size();
    }

    public long lastConnectedAtMillis() {
        return sessionManager.lastConnectedAtMillis();
    }

    public long lastSentAtMillis() {
        return lastSentAtMillis;
    }

    public long lastReceivedAtMillis() {
        return lastReceivedAtMillis;
    }

    private String displayKey(ResponseTracker tracker, ShetabCorrelationKey fallback) {
        if (tracker != null) {
            return tracker.correlationKey().display();
        }

        return fallback == null ? "" : fallback.display();
    }

    private String safeField(ISOMsg msg, int field) {
        try {
            return msg != null ? msg.getString(field) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String safeMti(ISOMsg msg) {
        try {
            return msg != null && msg.hasMTI() ? msg.getMTI() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Thread daemonThread(String name, Runnable runnable) {
        Thread thread = new Thread(runnable, name);
        thread.setDaemon(true);

        thread.setUncaughtExceptionHandler((t, e) ->
                log.error("Uncaught exception in Shetab thread provider={} thread={}",
                        config.provider(), t.getName(), e));

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
            long timeout = Math.max(1_000L, config.connectTimeoutMs() + config.socketTimeoutMs() + 500L);
            thread.join(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void logWireDebug(String direction, ISOMsg msg) {
        logWireDebug(direction, msg, -1L);
    }

    private void logWireDebug(String direction, ISOMsg msg, long elapsedMs) {
        if (!log.isDebugEnabled()) {
            return;
        }

        log.debug("Shetab {} provider={} message={}",
                direction,
                config.provider(),
                SafeIsoLogFormatter.format(SafeIsoLogFormatter.sanitize(msg), elapsedMs));
    }

    private void logIsoRequestBeforeSend(ISOMsg msg, List<String> requestKeys) {
        if (!log.isInfoEnabled()) {
            return;
        }

        ISOMsg safe = SafeIsoLogFormatter.sanitize(msg);
        log.trace("Shetab ISO request before send provider={} keys={} mti={} stan={} rrn={}",
                config.provider(),
                requestKeys,
                safeMti(safe),
                safeField(safe, 11),
                safeField(safe, 37));
    }

    private void logPackedIsoBeforeSend(ISOMsg msg) {
        if (!log.isDebugEnabled()) {
            return;
        }

        try {
            byte[] packed = SafeIsoLogFormatter.sanitize(msg).pack();

            log.debug("Shetab ISO packed provider={} totalLength={} hex={}",
                    config.provider(),
                    packed.length,
                    toHex(packed));

        } catch (Exception e) {
            log.trace("Could not pack Shetab ISO message for debug provider={}", config.provider(), e);
        }
    }

    private String toHex(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];

        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }

        return new String(hexChars);
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

    private record PendingRequest(
            ResponseTracker tracker,
            ISOMsg msg
    ) {
        String key() {
            return tracker.correlationKey().display();
        }
    }

    private sealed interface SenderCommand permits SendRequestCommand, ReconnectCommand {
    }

    private record SendRequestCommand(PendingRequest pending) implements SenderCommand {
    }

    private record ReconnectCommand(long failedGeneration) implements SenderCommand {
    }
}
