package ir.daneshrefah.scm.provider.shetab.tcp;

import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import ir.daneshrefah.scm.provider.shetab.iso.ShetabPackagerFactory;
import ir.daneshrefah.scm.provider.shetab.iso.log.SafeIsoLogFormatter;
import ir.daneshrefah.scm.provider.shetab.lease.ShetabEndpointLease;
import ir.daneshrefah.scm.provider.shetab.lease.ShetabEndpointLeaseManager;
import ir.daneshrefah.scm.provider.shetab.metrics.ShetabProviderMetrics;
import lombok.extern.slf4j.Slf4j;
import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.channel.ASCIIChannel;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ShetabIsoChannelClient {

    private static final int SHETAB_LENGTH_DIGITS = 4;

    private final ShetabResolvedConfig config;
    private final ShetabPackagerFactory packagerFactory;
    private final ShetabEndpointLeaseManager endpointLeaseManager;
    private final ShetabProviderMetrics.CounterSet metrics;

    private final ArrayBlockingQueue<PendingRequest> sendQueue;
    private final Map<String, ResponseTracker> responseMap = new ConcurrentHashMap<>();

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Object connectionLock = new Object();
    private final Object sendLock = new Object();

    private volatile ISOChannel channel;
    private volatile ShetabEndpointLease leasedEndpoint = ShetabEndpointLease.none();
    private volatile boolean connected;
    private volatile Throwable lastConnectionFailure;
    private volatile long lastConnectedAtMillis;
    private volatile long lastReceivedAtMillis;
    private volatile long lastSentAtMillis;

    private int sameEndpointFailureCount;
    private Thread senderThread;
    private Thread receiverThread;

    public ShetabIsoChannelClient(
            ShetabResolvedConfig config,
            ShetabPackagerFactory packagerFactory,
            ShetabEndpointLeaseManager endpointLeaseManager,
            ShetabProviderMetrics metrics
    ) {
        this.config = Objects.requireNonNull(config, "config");
        this.packagerFactory = Objects.requireNonNull(packagerFactory, "packagerFactory");
        this.endpointLeaseManager = Objects.requireNonNull(endpointLeaseManager, "endpointLeaseManager");
        this.metrics = Objects.requireNonNull(metrics, "metrics").provider(config.provider());

        int capacity = Math.max(1, config.queueCapacity());
        this.sendQueue = new ArrayBlockingQueue<>(capacity);
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

        synchronized (connectionLock) {
            closeChannel();
        }

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

        List<String> keys = correlationKeys(msg);
        if (keys.isEmpty()) {
            throw new IllegalArgumentException(
                    "Shetab request must contain field 11 (STAN) or field 37 (RRN) provider=" + config.provider()
            );
        }

        String primaryKey = keys.get(0);
        int effectiveTimeoutMs = Math.max(1, timeoutMs);
        long deadlineMillis = System.currentTimeMillis() + effectiveTimeoutMs;

        CompletableFuture<ISOMsg> future = new CompletableFuture<>();
        ResponseTracker tracker = new ResponseTracker(future, keys, deadlineMillis, System.nanoTime());

        registerTracker(tracker);

        boolean queued = false;

        try {
            PendingRequest pendingRequest = new PendingRequest(primaryKey, msg, 0, deadlineMillis);

            queued = sendQueue.offer(
                    pendingRequest,
                    Math.max(1, config.sendTimeoutMs()),
                    TimeUnit.MILLISECONDS
            );

            if (!queued) {
                removeTracker(tracker);
                metrics.queueRejected();
                throw new RejectedExecutionException("Shetab send queue is full provider=" + config.provider());
            }

            return future.get(effectiveTimeoutMs, TimeUnit.MILLISECONDS);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            removeTracker(tracker);

            throw new IllegalStateException(
                    "Interrupted while waiting Shetab response provider=" + config.provider()
                            + " key=" + primaryKey,
                    e
            );

        } catch (TimeoutException e) {
            removeTracker(tracker);
            metrics.timedOut();

            throw new IllegalStateException(
                    "Shetab response timed out provider=" + config.provider()
                            + " key=" + primaryKey
                            + " mti=" + safeMti(msg)
                            + " stan=" + safeField(msg, 11)
                            + " rrn=" + safeField(msg, 37),
                    e
            );

        } catch (ExecutionException e) {
            removeTracker(tracker);

            Throwable cause = e.getCause() != null ? e.getCause() : e;

            if (cause instanceof TimeoutException) {
                metrics.timedOut();
            } else {
                metrics.failed();
            }

            throw new IllegalStateException(
                    "Shetab request failed provider=" + config.provider()
                            + " key=" + primaryKey
                            + ", cause=" + rootMessage(cause),
                    cause
            );

        } catch (RuntimeException e) {
            removeTracker(tracker);

            if (!queued) {
                metrics.failed();
            }

            throw e;
        }
    }

    private void senderLoop() {
        while (running.get()) {
            PendingRequest pending = null;
            ISOChannel usedChannel = null;

            try {
                pending = sendQueue.take();

                if (isExpired(pending.deadlineMillis())) {
                    failPending(
                            pending.key(),
                            new TimeoutException("Shetab request expired before send provider="
                                    + config.provider() + " key=" + pending.key())
                    );
                    continue;
                }

                ResponseTracker tracker = responseMap.get(pending.key());
                if (tracker == null || tracker.future().isDone()) {
                    log.debug("Shetab sender ignored missing/done tracker provider={} key={}",
                            config.provider(), pending.key());
                    continue;
                }

                usedChannel = ensureConnected();

                if (usedChannel == null) {
                    throw new SocketException("Shetab channel is null provider=" + config.provider());
                }

                logIsoRequestBeforeSend(pending.msg(), tracker.keys());
                logPackedIsoBeforeSend(pending.msg());

                synchronized (sendLock) {
                    if (!running.get()) {
                        return;
                    }

                    if (usedChannel != channel || !connected) {
                        throw new SocketException("Shetab channel changed before send provider=" + config.provider());
                    }

                    usedChannel.send(pending.msg());
                }

                lastSentAtMillis = System.currentTimeMillis();

                metrics.sent();
                logWireDebug("sent", pending.msg());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;

            } catch (Exception e) {
                if (!running.get()) {
                    return;
                }

                metrics.failed();

                log.error("Shetab sender error provider={} pendingKey={} pendingMsg={}",
                        config.provider(),
                        pending != null ? pending.key() : null,
                        pending != null ? SafeIsoLogFormatter.format(pending.msg()) : null,
                        e);

                markDisconnected(usedChannel, e, false);

                if (pending != null) {
                    retryOrFail(pending, e);
                }
            }
        }
    }

    private void receiverLoop() {
        while (running.get()) {
            ISOChannel usedChannel = null;

            try {
                usedChannel = ensureConnected();

                if (usedChannel == null) {
                    throw new SocketException("Shetab channel is null before receive provider=" + config.provider());
                }

                ISOMsg response = usedChannel.receive();
                lastReceivedAtMillis = System.currentTimeMillis();

                logWireDebug("received", response);

                List<String> keys = correlationKeys(response);
                ResponseTracker tracker = findTracker(keys);

                if (tracker == null) {
                    log.trace("Shetab receive unmatched provider={} keys={} mti={} stan={} rrn={}",
                            config.provider(), keys, safeMti(response), safeField(response, 11), safeField(response, 37));
                    continue;
                }

                removeTracker(tracker);
                tracker.future().complete(response);

                metrics.received();
                metrics.addLatency(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - tracker.startedNanos()));

                log.trace("Shetab receive matched provider={} keys={} mti={} stan={} rrn={}",
                        config.provider(), keys, safeMti(response), safeField(response, 11), safeField(response, 37));

            } catch (SocketTimeoutException e) {
                cleanupExpiredTrackers();
                log.debug("Shetab receiver socket timeout provider={}", config.provider());

            } catch (IOException e) {
                if (running.get()) {
                    log.trace("Shetab receiver socket disconnected provider={} lastSentAt={} lastReceivedAt={}",
                            config.provider(), lastSentAtMillis, lastReceivedAtMillis, e);

                    markDisconnected(usedChannel, e, false);
                    sleep(config.reconnectDelayMs());
                }

            } catch (Exception e) {
                if (running.get()) {
                    log.trace("Shetab receiver error provider={}", config.provider(), e);

                    markDisconnected(usedChannel, e, false);
                    sleep(config.reconnectDelayMs());
                }
            }
        }
    }

    private ISOChannel ensureConnected() {
        if (!running.get()) {
            throw new IllegalStateException("Shetab client is stopped provider=" + config.provider());
        }

        ISOChannel current = channel;

        /*
         * Important:
         * Do not aggressively call current.isConnected() here.
         * In jPOS, isConnected() can be misleading around receive timeout / socket state.
         * Reconnect must happen only when channel is null or when markDisconnected() explicitly marked it disconnected.
         */
        if (connected && current != null) {
            return current;
        }

        synchronized (connectionLock) {
            current = channel;

            if (connected && current != null) {
                return current;
            }

            return connectUntilSuccess();
        }
    }

    private ISOChannel connectUntilSuccess() {
        while (running.get()) {
            ISOChannel newChannel = null;

            try {
                /*
                 * Do not close the existing channel here just because isConnected() says false.
                 * Existing channel is closed only by markDisconnected() after a real send/receive error.
                 */

                if (isEmptyLease(leasedEndpoint)) {
                    leasedEndpoint = endpointLeaseManager.acquire(config);
                    sameEndpointFailureCount = 0;

                    log.trace("Acquired Shetab endpoint lease provider={} endpoint={}",
                            config.provider(), leasedEndpoint.endpoint());
                }

                newChannel = createChannel(leasedEndpoint);
                newChannel.connect();

                if (!running.get()) {
                    disconnectQuietly(newChannel);
                    return null;
                }

                ISOChannel previous = channel;

                channel = newChannel;
                connected = true;
                lastConnectionFailure = null;
                sameEndpointFailureCount = 0;
                lastConnectedAtMillis = System.currentTimeMillis();

                if (previous != null && previous != newChannel) {
                    disconnectQuietly(previous);
                }

                log.trace("Connected Shetab ISOChannel provider={} remoteEndpoint={}",
                        config.provider(), leasedEndpoint.endpoint());

                return newChannel;

            } catch (Exception e) {
                disconnectQuietly(newChannel);

                if (!running.get()) {
                    return null;
                }

                connected = false;
                lastConnectionFailure = e;
                sameEndpointFailureCount++;

                log.error("Shetab connection failed provider={} remoteEndpoint={} attempt={}/{} retryInMs={}",
                        config.provider(),
                        leasedEndpoint != null ? leasedEndpoint.endpoint() : null,
                        sameEndpointFailureCount,
                        maxSameEndpointReconnectAttempts(),
                        config.reconnectDelayMs(),
                        e);

                if (sameEndpointFailureCount >= maxSameEndpointReconnectAttempts()) {
                    releaseLease();
                }

                sleep(config.reconnectDelayMs());
            }
        }

        return null;
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

    private void retryOrFail(PendingRequest pending, Throwable error) {
        ResponseTracker tracker = responseMap.get(pending.key());
        if (tracker == null || tracker.future().isDone()) {
            return;
        }

        if (isExpired(pending.deadlineMillis())) {
            removeTracker(tracker);
            tracker.future().completeExceptionally(
                    new TimeoutException("Shetab request expired provider="
                            + config.provider() + " key=" + pending.key())
            );
            return;
        }

        if (!running.get() || pending.sendAttempts() >= maxSameEndpointReconnectAttempts()) {
            removeTracker(tracker);
            tracker.future().completeExceptionally(error);
            return;
        }

        PendingRequest retry = new PendingRequest(
                pending.key(),
                pending.msg(),
                pending.sendAttempts() + 1,
                pending.deadlineMillis()
        );

        if (!sendQueue.offer(retry)) {
            removeTracker(tracker);
            metrics.queueRejected();
            tracker.future().completeExceptionally(
                    new RejectedExecutionException("Shetab send queue is full provider=" + config.provider())
            );
            return;
        }

        log.trace("Requeued Shetab request after send failure provider={} key={} attempt={}/{}",
                config.provider(), pending.key(), retry.sendAttempts(), maxSameEndpointReconnectAttempts());
    }

    private void registerTracker(ResponseTracker tracker) {
        List<String> registeredKeys = new ArrayList<>();

        for (String key : tracker.keys()) {
            ResponseTracker existing = responseMap.putIfAbsent(key, tracker);

            if (existing != null) {
                for (String registeredKey : registeredKeys) {
                    responseMap.remove(registeredKey, tracker);
                }

                throw new IllegalStateException("Duplicate Shetab request correlation key provider="
                        + config.provider() + " key=" + key);
            }

            registeredKeys.add(key);
        }
    }

    private ResponseTracker findTracker(List<String> keys) {
        for (String key : keys) {
            ResponseTracker tracker = responseMap.get(key);
            if (tracker != null) {
                return tracker;
            }
        }

        return null;
    }

    private void removeTracker(ResponseTracker tracker) {
        if (tracker == null) {
            return;
        }

        for (String key : tracker.keys()) {
            responseMap.remove(key, tracker);
        }
    }

    private void failPending(String primaryKey, Throwable error) {
        ResponseTracker tracker = responseMap.get(primaryKey);
        if (tracker == null) {
            return;
        }

        removeTracker(tracker);
        tracker.future().completeExceptionally(error);
    }

    private void failAllPending(Throwable error) {
        List<ResponseTracker> trackers = uniqueTrackers();

        for (ResponseTracker tracker : trackers) {
            removeTracker(tracker);
            tracker.future().completeExceptionally(error);
        }

        responseMap.clear();
    }

    private void drainSendQueue(Throwable error) {
        PendingRequest pending;

        while ((pending = sendQueue.poll()) != null) {
            failPending(pending.key(), error);
        }
    }

    private void cleanupExpiredTrackers() {
        long now = System.currentTimeMillis();

        for (ResponseTracker tracker : uniqueTrackers()) {
            if (tracker.deadlineMillis() <= now) {
                removeTracker(tracker);

                if (!tracker.future().isDone()) {
                    tracker.future().completeExceptionally(
                            new TimeoutException("Shetab response timed out provider="
                                    + config.provider()
                                    + " keys=" + tracker.keys())
                    );
                    metrics.timedOut();
                }
            }
        }
    }

    private List<ResponseTracker> uniqueTrackers() {
        Map<ResponseTracker, Boolean> seen = new IdentityHashMap<>();
        List<ResponseTracker> trackers = new ArrayList<>();

        for (ResponseTracker tracker : responseMap.values()) {
            if (!seen.containsKey(tracker)) {
                seen.put(tracker, Boolean.TRUE);
                trackers.add(tracker);
            }
        }

        return trackers;
    }

    private void markDisconnected(ISOChannel failedChannel, Throwable error, boolean releaseLease) {
        synchronized (connectionLock) {
            lastConnectionFailure = error;

            ISOChannel current = channel;

            /*
             * Only the thread that failed on the active channel is allowed to close it.
             * If another thread has already created a newer channel, do not close the newer one.
             */
            if (failedChannel == null || failedChannel == current) {
                connected = false;

                if (current != null) {
                    channel = null;

                    log.trace("Closing Shetab socket provider={} endpoint={} cause={}",
                            config.provider(),
                            leasedEndpoint != null ? leasedEndpoint.endpoint() : null,
                            rootMessage(error));

                    disconnectQuietly(current);
                }
            } else {
                log.debug("Ignored stale Shetab channel disconnect provider={} cause={}",
                        config.provider(), rootMessage(error));

                disconnectQuietly(failedChannel);
            }

            if (releaseLease) {
                releaseLease();
            }
        }
    }

    private void closeChannel() {
        ISOChannel current = channel;

        connected = false;
        channel = null;

        if (current != null) {
            log.trace("Closing Shetab socket provider={} endpoint={}",
                    config.provider(),
                    leasedEndpoint != null ? leasedEndpoint.endpoint() : null);
        }

        disconnectQuietly(current);
        releaseLease();
    }

    private void disconnectQuietly(ISOChannel current) {
        if (current == null) {
            return;
        }

        try {
            current.disconnect();
        } catch (Exception ignored) {
        }
    }

    private void releaseLease() {
        try {
            if (leasedEndpoint != null) {
                leasedEndpoint.close();
            }
        } catch (Exception ignored) {
        }

        leasedEndpoint = ShetabEndpointLease.none();
        sameEndpointFailureCount = 0;
    }

    public boolean isHealthy() {
        return running.get()
                && connected
                && channel != null
                && lastConnectionFailure == null;
    }

    public Throwable lastConnectionFailure() {
        return lastConnectionFailure;
    }

    public int pendingResponseCount() {
        return uniqueTrackers().size();
    }

    public int queuedRequestCount() {
        return sendQueue.size();
    }

    public long lastConnectedAtMillis() {
        return lastConnectedAtMillis;
    }

    public long lastSentAtMillis() {
        return lastSentAtMillis;
    }

    public long lastReceivedAtMillis() {
        return lastReceivedAtMillis;
    }

    private boolean isEmptyLease(ShetabEndpointLease endpointLease) {
        return endpointLease == null
                || endpointLease.endpoint() == null
                || endpointLease.endpoint().isBlank();
    }

    private int maxSameEndpointReconnectAttempts() {
        return Math.max(1, config.sameEndpointReconnectAttempts());
    }

    private boolean isExpired(long deadlineMillis) {
        return System.currentTimeMillis() >= deadlineMillis;
    }

    private List<String> correlationKeys(ISOMsg msg) {
        String stan = cleanField(safeField(msg, 11));
        String rrn = cleanField(safeField(msg, 37));

        List<String> keys = new ArrayList<>(3);

        if (stan != null && rrn != null) {
            keys.add(stan + "|" + rrn);
        }

        if (stan != null) {
            keys.add(stan);
        }

        if (rrn != null) {
            keys.add("rrn:" + rrn);
        }

        return List.copyOf(keys);
    }

    private String cleanField(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
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

    private void sleep(long ms) {
        try {
            Thread.sleep(Math.max(1, ms));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
                SafeIsoLogFormatter.format(msg, elapsedMs));
    }

    private void logIsoRequestBeforeSend(ISOMsg msg, List<String> requestKeys) {
        if (!log.isInfoEnabled()) {
            return;
        }

        log.trace("Shetab ISO request before send provider={} keys={} mti={} stan={} rrn={}",
                config.provider(),
                requestKeys,
                safeMti(msg),
                safeField(msg, 11),
                safeField(msg, 37));
    }

    private void logPackedIsoBeforeSend(ISOMsg msg) {
        if (!log.isDebugEnabled()) {
            return;
        }

        try {
            byte[] packed = msg.pack();

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
            String key,
            ISOMsg msg,
            int sendAttempts,
            long deadlineMillis
    ) {
    }

    private record ResponseTracker(
            CompletableFuture<ISOMsg> future,
            List<String> keys,
            long deadlineMillis,
            long startedNanos
    ) {
    }
}