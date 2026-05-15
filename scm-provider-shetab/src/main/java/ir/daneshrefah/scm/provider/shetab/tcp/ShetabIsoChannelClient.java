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

import java.net.SocketTimeoutException;
import java.net.SocketException;
import java.util.*;
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

    private volatile ISOChannel channel;
    private volatile ShetabEndpointLease leasedEndpoint = ShetabEndpointLease.none();
    private volatile boolean connected;
    private volatile Throwable lastConnectionFailure;
    private int sameEndpointFailureCount;
    private Thread senderThread;
    private Thread receiverThread;

    public ShetabIsoChannelClient(
            ShetabResolvedConfig config,
            ShetabPackagerFactory packagerFactory,
            ShetabEndpointLeaseManager endpointLeaseManager,
            ShetabProviderMetrics metrics
    ) {
        this.config = config;
        this.packagerFactory = packagerFactory;
        this.endpointLeaseManager = endpointLeaseManager;
        this.metrics = metrics.provider(config.provider());
        this.sendQueue = new ArrayBlockingQueue<>(config.queueCapacity());
    }

    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        senderThread = thread("shetab-sender-" + config.provider(), this::senderLoop);
        receiverThread = thread("shetab-receiver-" + config.provider(), this::receiverLoop);
        senderThread.start();
        receiverThread.start();
        log.info("Started Shetab ISOChannel client provider={} endpointCount={}",
                config.provider(), config.endpoints() != null ? config.endpoints().size() : 0);
    }

    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }
        if (senderThread != null) {
            senderThread.interrupt();
        }
        if (receiverThread != null) {
            receiverThread.interrupt();
        }
        synchronized (connectionLock) {
            closeChannel();
        }
        responseMap.forEach((key, tracker) -> tracker.future().completeExceptionally(new IllegalStateException("Shetab client stopped")));
        responseMap.clear();
        joinQuietly(senderThread);
        joinQuietly(receiverThread);
        log.info("Stopped Shetab ISOChannel client provider={}", config.provider());
    }

    public ISOMsg request(ISOMsg msg, int timeoutMs) {
        metrics.submitted();
        List<String> keys = correlationKeys(msg);
        if (keys.isEmpty()) {
            throw new IllegalArgumentException("Shetab request must contain field 11 (STAN) or field 37 (RRN) provider=" + config.provider());
        }
        String key = keys.getFirst();
        CompletableFuture<ISOMsg> future = new CompletableFuture<>();
        ResponseTracker tracker = new ResponseTracker(future, keys);
        registerTracker(tracker);

        try {
            boolean accepted = sendQueue.offer(new PendingRequest(key, msg, 0), config.sendTimeoutMs(), TimeUnit.MILLISECONDS);
            if (!accepted) {
                removeTracker(tracker);
                metrics.queueRejected();
                throw new RejectedExecutionException("Shetab send queue is full provider=" + config.provider());
            }
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            removeTracker(tracker);
            throw new IllegalStateException("Interrupted while waiting Shetab response provider=" + config.provider(), e);
        } catch (TimeoutException e) {
            removeTracker(tracker);
            metrics.timedOut();
            throw new IllegalStateException("Shetab response timed out provider=" + config.provider() + " key=" + key, e);
        } catch (ExecutionException e) {
            removeTracker(tracker);
            metrics.failed();
            throw new IllegalStateException("Shetab request failed provider=" + config.provider() + " key=" + key, e.getCause());
        }
    }

    private void senderLoop() {
        while (running.get()) {
            PendingRequest pending = null;
            ISOMsg request = null;
            try {
                pending = sendQueue.take();
                if (!responseMap.containsKey(pending.key())) {
                    log.warn("Not exist pending response for key={}", pending.key());
                    continue;
                }
                ensureConnected();
                ISOChannel currentChannel = channel;
                if (currentChannel == null || !currentChannel.isConnected()) {
                    throw new SocketException("Shetab channel is not connected");
                }
                request = pending.msg();
                currentChannel.send(request);
                metrics.sent();
                log.info("Shetab sent provider={}, requestMsg={}", config.provider(), SafeIsoLogFormatter.format(request));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {

                if (!running.get()) {
                    log.debug("Shetab sender stopped provider={}, pendingMsg={}", config.provider(), SafeIsoLogFormatter.format(request), e);
                    return;
                }
                metrics.failed();
                log.error("Shetab sender error provider={}, pendingMsg={}", config.provider(), SafeIsoLogFormatter.format(request), e);
                markDisconnected(e, false);
                if (pending != null) {
                    retryOrFail(pending, e);
                }
            }
        }
    }

    private void receiverLoop() {
        while (running.get()) {
            try {
                ensureConnected();
                ISOMsg response = channel.receive();
                log.info("Shetab receive provider={}, responseMsg={}", config.provider(), SafeIsoLogFormatter.format(response));
                List<String> keys = correlationKeys(response);
                ResponseTracker tracker = findTracker(keys);
                if (tracker != null) {
                    removeTracker(tracker);
                    tracker.future().complete(response);
                    metrics.received();
                    log.info("Shetab receive provider={} keys={} mti={} stan={} rrn={}",
                            config.provider(), keys, safeMti(response), safeField(response, 11), safeField(response, 37));
                } else {
                    log.warn("Shetab receive unmatched provider={} keys={} mti={} stan={} rrn={}",
                            config.provider(), keys, safeMti(response), safeField(response, 11), safeField(response, 37));
                }
            } catch (SocketTimeoutException e) {
                log.debug("Shetab receiver socket timeout provider={}", config.provider());
            } catch (SocketException e) {
                if (running.get()) {
                    log.warn("Shetab receiver socket error provider={}", config.provider(), e);
                    markDisconnected(e, false);
                }
            } catch (Exception e) {
                if (running.get()) {
                    log.warn("Shetab receiver error provider={}", config.provider(), e);
                    markDisconnected(e, false);
                }
            }
        }
    }

    private void ensureConnected() {
        if (!running.get()) {
            throw new IllegalStateException("Shetab client is stopped provider=" + config.provider());
        }
        if (connected && channel != null && channel.isConnected()) {
            return;
        }
        synchronized (connectionLock) {
            if (!running.get()) {
                throw new IllegalStateException("Shetab client is stopped provider=" + config.provider());
            }
            if (connected && channel != null && channel.isConnected()) {
                return;
            }
            connect();
        }
    }

    private void connect() {
        while (running.get()) {
            ISOChannel newChannel = null;
            try {
                if (isEmptyLease(leasedEndpoint)) {
                    leasedEndpoint = endpointLeaseManager.acquire(config);
                    sameEndpointFailureCount = 0;
                    log.info("Acquired Shetab endpoint lease provider={} endpoint={}",
                            config.provider(), leasedEndpoint.endpoint());
                }
                closeSocket();
                newChannel = createChannel(leasedEndpoint);
                newChannel.connect();
                if (!running.get()) {
                    disconnectQuietly(newChannel);
                    return;
                }
                channel = newChannel;
                connected = true;
                lastConnectionFailure = null;
                sameEndpointFailureCount = 0;
                log.info("Connected Shetab ISOChannel provider={} remoteEndpoint={}",
                        config.provider(), leasedEndpoint.endpoint());
                return;
            } catch (Exception e) {
                disconnectQuietly(newChannel);
                if (!running.get()) {
                    return;
                }
                connected = false;
                lastConnectionFailure = e;
                sameEndpointFailureCount++;
                log.error("Shetab connection failed provider={} remoteEndpoint={} attempt={}/{} retryInMs={}",
                        config.provider(), leasedEndpoint.endpoint(), sameEndpointFailureCount,
                        maxSameEndpointReconnectAttempts(), config.reconnectDelayMs(), e);
                closeSocket();
                if (sameEndpointFailureCount >= maxSameEndpointReconnectAttempts()) {
                    releaseLease();
                }
                sleep(config.reconnectDelayMs());
            }
        }
    }

    private ISOChannel createChannel(ShetabEndpointLease endpointLease) throws Exception {
        String remoteHost = endpointLease.remoteHost();
        int remotePort = endpointLease.remotePort();
        if (remoteHost == null || remoteHost.isBlank() || remotePort <= 0) {
            throw new IllegalStateException("Invalid leased endpoint for provider " + config.provider() + ": " + endpointLease.endpoint());
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

    private void markDisconnected(Throwable error, boolean releaseLease) {
        synchronized (connectionLock) {
            lastConnectionFailure = error;
            closeSocket();
            if (releaseLease) {
                releaseLease();
            }
        }
    }

    private void closeChannel() {
        connected = false;
        closeSocket();
        releaseLease();
    }

    private void closeSocket() {
        connected = false;
        ISOChannel current = channel;
        channel = null;
        disconnectQuietly(current);
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
        leasedEndpoint.close();
        leasedEndpoint = ShetabEndpointLease.none();
        sameEndpointFailureCount = 0;
    }

    public boolean isHealthy() {
        return running.get() && connected && channel != null && channel.isConnected() && lastConnectionFailure == null;
    }

    public Throwable lastConnectionFailure() {
        return lastConnectionFailure;
    }

    private boolean isEmptyLease(ShetabEndpointLease endpointLease) {
        return endpointLease == null || endpointLease.endpoint() == null || endpointLease.endpoint().isBlank();
    }

    private int maxSameEndpointReconnectAttempts() {
        return Math.max(1, config.sameEndpointReconnectAttempts());
    }

    private void retryOrFail(PendingRequest pending, Throwable error) {
        ResponseTracker tracker = responseMap.get(pending.key());
        if (tracker == null || tracker.future().isDone()) {
            return;
        }
        if (!running.get() || pending.sendAttempts() >= maxSameEndpointReconnectAttempts()) {
            removeTracker(tracker);
            tracker.future().completeExceptionally(error);
            return;
        }
        PendingRequest retry = new PendingRequest(pending.key(), pending.msg(), pending.sendAttempts() + 1);
        if (!sendQueue.offer(retry)) {
            removeTracker(tracker);
            metrics.queueRejected();
            tracker.future().completeExceptionally(new RejectedExecutionException("Shetab send queue is full provider=" + config.provider()));
            return;
        }
        log.warn("Requeued Shetab request after send failure provider={} key={} attempt={}/{}",
                config.provider(), pending.key(), retry.sendAttempts(), maxSameEndpointReconnectAttempts());
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
        tracker.keys().forEach(key -> responseMap.remove(key, tracker));
    }

    private void registerTracker(ResponseTracker tracker) {
        for (String key : tracker.keys()) {
            ResponseTracker existing = responseMap.putIfAbsent(key, tracker);
            if (existing != null) {
                removeTracker(tracker);
                throw new IllegalStateException("Duplicate Shetab request correlation key provider=" + config.provider() + " key=" + key);
            }
        }
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

    private Thread thread(String name, Runnable runnable) {
        Thread thread = new Thread(runnable, name);
        thread.setDaemon(true);
        return thread;
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void joinQuietly(Thread thread) {
        if (thread == null || thread == Thread.currentThread()) {
            return;
        }
        try {
            thread.join(Math.max(1_000L, config.connectTimeoutMs() + config.socketTimeoutMs() + 500L));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private record PendingRequest(String key, ISOMsg msg, int sendAttempts) {
    }

    private record ResponseTracker(CompletableFuture<ISOMsg> future, List<String> keys) {
    }
}
