package ir.daneshrefah.scm.provider.nab.tcp;

import ir.daneshrefah.scm.provider.nab.config.NabResolvedConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.time.Clock;

@Slf4j
final class NabPooledConnection implements AutoCloseable {
    private final NabEndpointAddress endpoint;
    private final Charset charset;
    private final Clock clock;
    private final long createdAtMs;
    private final Socket socket;
    private final BufferedInputStream input;
    private final BufferedOutputStream output;
    private volatile long lastUsedAtMs;

    private NabPooledConnection(
            NabEndpointAddress endpoint,
            Charset charset,
            Clock clock,
            long createdAtMs,
            Socket socket,
            BufferedInputStream input,
            BufferedOutputStream output
    ) {
        this.endpoint = endpoint;
        this.charset = charset;
        this.clock = clock;
        this.createdAtMs = createdAtMs;
        this.socket = socket;
        this.input = input;
        this.output = output;
        this.lastUsedAtMs = createdAtMs;
    }

    static NabPooledConnection open(NabEndpointAddress endpoint, NabResolvedConfig config, Charset charset, Clock clock) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(endpoint.host(), endpoint.port()), config.connectTimeoutMs());
            socket.setSoTimeout(config.socketTimeoutMs());
            long now = clock.millis();
            log.info("Opened NAB pooled connection provider={} endpoint={}", config.provider(), endpoint.value());
            return new NabPooledConnection(
                    endpoint,
                    charset,
                    clock,
                    now,
                    socket,
                    new BufferedInputStream(socket.getInputStream()),
                    new BufferedOutputStream(socket.getOutputStream())
            );
        } catch (Exception e) {
            throw new IllegalStateException("Could not open NAB pooled connection endpoint=" + endpoint.value(), e);
        }
    }

    String request(NabResolvedConfig config, String protocol, String body) {
        try {
            write(protocol);
            String ack = readFixed(config.ackLengthBytes());
            logWire(config, "received-ack", ack);
            if (!ack.endsWith("00000")) {
                throw new IllegalStateException("NAB dispatcher acknowledge is not successful: " + ack);
            }
            write(body);
            String response = readResponse(config);
            markUsed();
            return response;
        } catch (SocketTimeoutException e) {
            throw new IllegalStateException("NAB response timed out provider=" + config.provider()
                    + " endpoint=" + endpoint.value(), e);
        } catch (Exception e) {
            throw new IllegalStateException("NAB request failed provider=" + config.provider()
                    + " endpoint=" + endpoint.value(), e);
        }
    }

    boolean reusable(NabResolvedConfig config) {
        if (socket.isClosed() || !socket.isConnected() || socket.isInputShutdown() || socket.isOutputShutdown()) {
            return false;
        }
        long now = clock.millis();
        return now - createdAtMs <= config.connectionPool().maxLifeTimeMs();
    }

    boolean stale(NabResolvedConfig config) {
        return !reusable(config);
    }

    String endpointValue() {
        return endpoint.value();
    }

    long idleForMs() {
        return clock.millis() - lastUsedAtMs;
    }

    long ageMs() {
        return clock.millis() - createdAtMs;
    }

    private void write(String value) throws Exception {
        output.write(value.getBytes(charset));
        output.flush();
    }

    private String readFixed(int lengthBytes) throws Exception {
        byte[] bytes = input.readNBytes(lengthBytes);
        if (bytes.length < lengthBytes) {
            throw new EOFException("Expected " + lengthBytes + " bytes but received " + bytes.length);
        }
        return new String(bytes, charset);
    }

    private String readResponse(NabResolvedConfig config) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[1024];
        socket.setSoTimeout(config.responseTimeoutMs());
        int read = input.read(chunk);
        if (read < 0) {
            return "";
        }
        buffer.write(chunk, 0, read);

        socket.setSoTimeout(config.responseIdleTimeoutMs());
        while (true) {
            try {
                read = input.read(chunk);
                if (read < 0) {
                    break;
                }
                buffer.write(chunk, 0, read);
            } catch (SocketTimeoutException e) {
                break;
            }
        }
        return buffer.toString(charset);
    }

    private void markUsed() {
        lastUsedAtMs = clock.millis();
    }

    private void logWire(NabResolvedConfig config, String direction, String content) {
        if (config.wireLogEnabled()) {
            log.info("NAB wire provider={} endpoint={} direction={} content=[{}]",
                    config.provider(), endpoint.value(), direction, content);
        } else {
            log.debug("NAB wire provider={} endpoint={} direction={} chars={}",
                    config.provider(), endpoint.value(), direction, content.length());
        }
    }

    @Override
    public void close() {
        try {
            socket.close();
        } catch (Exception e) {
            log.debug("Could not close NAB pooled connection endpoint={}", endpoint.value(), e);
        }
    }
}
