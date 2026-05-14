package ir.daneshrefah.scm.provider.shetab.tcp;

import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import ir.daneshrefah.scm.provider.shetab.iso.ShetabPackagerFactory;
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
import java.util.Map;
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
    private final Map<String, CompletableFuture<ISOMsg>> responseMap = new ConcurrentHashMap<>();
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
        closeChannel();
        if (senderThread != null) {
            senderThread.interrupt();
        }
        if (receiverThread != null) {
            receiverThread.interrupt();
        }
        responseMap.forEach((key, future) -> future.completeExceptionally(new IllegalStateException("Shetab client stopped")));
        responseMap.clear();
        log.info("Stopped Shetab ISOChannel client provider={}", config.provider());
    }

    public ISOMsg request(ISOMsg msg, int timeoutMs) {
        metrics.submitted();
        String key = correlationKey(msg);
        CompletableFuture<ISOMsg> future = new CompletableFuture<>();
        responseMap.put(key, future);

        try {
            boolean accepted = sendQueue.offer(new PendingRequest(key, msg), config.sendTimeoutMs(), TimeUnit.MILLISECONDS);
            if (!accepted) {
                responseMap.remove(key);
                metrics.queueRejected();
                throw new RejectedExecutionException("Shetab send queue is full provider=" + config.provider());
            }
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            responseMap.remove(key);
            throw new IllegalStateException("Interrupted while waiting Shetab response provider=" + config.provider(), e);
        } catch (TimeoutException e) {
            responseMap.remove(key);
            metrics.timedOut();
            throw new IllegalStateException("Shetab response timed out provider=" + config.provider() + " key=" + key, e);
        } catch (ExecutionException e) {
            responseMap.remove(key);
            metrics.failed();
            throw new IllegalStateException("Shetab request failed provider=" + config.provider() + " key=" + key, e.getCause());
        }
    }

    private void senderLoop() {
        while (running.get()) {
            try {
                PendingRequest pending = sendQueue.take();
                ensureConnected();
                channel.send(pending.msg());
                metrics.sent();
                log.info("Shetab SEND provider={} key={} mti={} stan={} rrn={} queueSize={}",
                        config.provider(), pending.key(), safeMti(pending.msg()), safeField(pending.msg(), 11),
                        safeField(pending.msg(), 37), sendQueue.size());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                metrics.failed();
                log.error("Shetab sender error provider={}", config.provider(), e);
                failAll(e);
                markDisconnected(e, false);
            }
        }
    }

    private void receiverLoop() {
        while (running.get()) {
            try {
                ensureConnected();
                ISOMsg response = channel.receive();
                String key = correlationKey(response);
                CompletableFuture<ISOMsg> future = responseMap.remove(key);
                if (future != null) {
                    future.complete(response);
                    metrics.received();
                    log.info("Shetab RECEIVE provider={} key={} mti={} stan={} rrn={}",
                            config.provider(), key, safeMti(response), safeField(response, 11), safeField(response, 37));
                } else {
                    log.warn("Shetab RECEIVE unmatched provider={} key={} mti={} stan={} rrn={}",
                            config.provider(), key, safeMti(response), safeField(response, 11), safeField(response, 37));
                }
            } catch (SocketTimeoutException e) {
                log.debug("Shetab receiver socket timeout provider={}", config.provider());
            } catch (SocketException e) {
                if (running.get()) {
                    log.warn("Shetab receiver socket error provider={}", config.provider(), e);
                    failAll(e);
                    markDisconnected(e, false);
                }
            } catch (Exception e) {
                if (running.get()) {
                    log.warn("Shetab receiver error provider={}", config.provider(), e);
                    failAll(e);
                    markDisconnected(e, false);
                }
            }
        }
    }

    private void ensureConnected() {
        if (connected && channel != null && channel.isConnected()) {
            return;
        }
        synchronized (connectionLock) {
            if (connected && channel != null && channel.isConnected()) {
                return;
            }
            connect();
        }
    }

    private void connect() {
        while (running.get()) {
            try {
                if (isEmptyLease(leasedEndpoint)) {
                    leasedEndpoint = endpointLeaseManager.acquire(config);
                    sameEndpointFailureCount = 0;
                    log.info("Acquired Shetab endpoint lease provider={} endpoint={}",
                            config.provider(), leasedEndpoint.endpoint());
                }
                closeSocket();
                channel = createChannel(leasedEndpoint);
                channel.connect();
                connected = true;
                lastConnectionFailure = null;
                sameEndpointFailureCount = 0;
                log.info("Connected Shetab ISOChannel provider={} remoteEndpoint={}",
                        config.provider(), leasedEndpoint.endpoint());
                return;
            } catch (Exception e) {
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
        if (current != null) {
            try {
                current.disconnect();
            } catch (Exception ignored) {
            }
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

    private void failAll(Throwable error) {
        responseMap.forEach((key, future) -> future.completeExceptionally(error));
        responseMap.clear();
    }

    private String correlationKey(ISOMsg msg) {
        String stan = safeField(msg, 11);
        String rrn = safeField(msg, 37);
        if (rrn != null && !rrn.isBlank()) {
            return stan + "|" + rrn.trim();
        }
        return stan == null ? "" : stan;
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

    private record PendingRequest(String key, ISOMsg msg) {
    }
}
