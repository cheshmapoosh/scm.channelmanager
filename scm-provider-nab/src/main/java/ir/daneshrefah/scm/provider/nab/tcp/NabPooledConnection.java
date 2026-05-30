package ir.daneshrefah.scm.provider.nab.tcp;

import ir.daneshrefah.scm.provider.nab.config.NabResolvedConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;

@Slf4j
final class NabPooledConnection implements AutoCloseable {
    private static final String NAB_SUCCESS_CODE = "00000";
    private static final String NAB_SUCCESS_LIST_CODE = "10000";

    private final NabEndpointAddress endpoint;
    private final Charset charset;
    private final Socket socket;
    private final BufferedInputStream input;
    private final BufferedOutputStream output;

    private NabPooledConnection(
            NabEndpointAddress endpoint,
            Charset charset,
            Socket socket,
            BufferedInputStream input,
            BufferedOutputStream output
    ) {
        this.endpoint = endpoint;
        this.charset = charset;
        this.socket = socket;
        this.input = input;
        this.output = output;
    }

    static NabPooledConnection open(NabEndpointAddress endpoint, NabResolvedConfig config, Charset charset) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(endpoint.host(), endpoint.port()), config.connectTimeoutMs());
            socket.setSoTimeout(config.socketTimeoutMs());
            log.info("Opened NAB connection provider={} endpoint={}", config.provider(), endpoint.value());
            return new NabPooledConnection(
                    endpoint,
                    charset,
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
            String ack = readAck(config);
            logWire(config, "received-ack", ack);
            if (!ack.endsWith(NAB_SUCCESS_CODE)) {
                throw new IllegalStateException("NAB dispatcher acknowledge is not successful: " + ack);
            }
            write(body);
            String response = readResponse(config);
            return response;
        } catch (SocketTimeoutException e) {
            throw new IllegalStateException("NAB response timed out provider=" + config.provider()
                    + " endpoint=" + endpoint.value(), e);
        } catch (Exception e) {
            throw new IllegalStateException("NAB request failed provider=" + config.provider()
                    + " endpoint=" + endpoint.value(), e);
        }
    }

    String endpointValue() {
        return endpoint.value();
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

    private String readAck(NabResolvedConfig config) throws Exception {
        return readFramePayload(config.ackLengthBytes(), config.socketTimeoutMs());
    }

    private String readResponse(NabResolvedConfig config) throws Exception {
        int lengthBytes = config.ackLengthBytes();
        String firstFramePayload = readFramePayload(lengthBytes, config.responseTimeoutMs());
        if (!isListFrame(firstFramePayload)) {
            return firstFramePayload;
        }

        StringBuilder listPayload = new StringBuilder(firstFramePayload);
        while (true) {
            String nextFramePayload = readFramePayload(lengthBytes, nextFrameTimeout(config));
            if (nextFramePayload.isEmpty()) {
                continue;
            }
            if (isListTerminatorFrame(nextFramePayload)) {
                return listPayload.toString();
            }
            if (!isListFrame(nextFramePayload)) {
                throw new IllegalStateException("NAB list response expected actionCode " + NAB_SUCCESS_LIST_CODE
                        + " or terminator " + NAB_SUCCESS_CODE + " but got frame: " + nextFramePayload);
            }
            listPayload.append('\n').append(nextFramePayload);
        }
    }

    private int nextFrameTimeout(NabResolvedConfig config) {
        int idleTimeout = config.responseIdleTimeoutMs();
        return idleTimeout > 0 ? idleTimeout : config.responseTimeoutMs();
    }

    private boolean isListFrame(String payload) {
        return payload != null && payload.startsWith(NAB_SUCCESS_LIST_CODE);
    }

    private boolean isListTerminatorFrame(String payload) {
        return NAB_SUCCESS_CODE.equals(payload);
    }

    private String readFramePayload(int lengthBytes, int timeoutMs) throws Exception {
        if (lengthBytes < 1) {
            throw new IllegalStateException("NAB length prefix bytes must be positive. length=" + lengthBytes);
        }
        if (timeoutMs < 1) {
            throw new IllegalStateException("NAB frame timeout must be positive. timeoutMs=" + timeoutMs);
        }
        socket.setSoTimeout(timeoutMs);

        String lengthPrefix = readFixed(lengthBytes);
        int payloadLength = parseLength(lengthPrefix, lengthBytes);
        if (log.isTraceEnabled()) {
            log.trace("NAB frame header endpoint={} lengthPrefix={} payloadLength={}",
                    endpoint.value(), lengthPrefix, payloadLength);
        }
        if (payloadLength == 0) {
            return "";
        }

        byte[] payload = input.readNBytes(payloadLength);
        if (payload.length < payloadLength) {
            throw new EOFException("Expected " + payloadLength + " bytes but received " + payload.length);
        }
        return new String(payload, charset);
    }

    private int parseLength(String prefix, int lengthBytes) {
        if (prefix == null || prefix.length() != lengthBytes) {
            throw new IllegalStateException("NAB length prefix has invalid size: " + prefix);
        }
        for (int i = 0; i < prefix.length(); i++) {
            if (!Character.isDigit(prefix.charAt(i))) {
                throw new IllegalStateException("NAB length prefix contains non-digit characters: " + prefix);
            }
        }
        try {
            int length = Integer.parseInt(prefix);
            if (length < 0) {
                throw new IllegalStateException("NAB length prefix is negative: " + prefix);
            }
            return length;
        } catch (NumberFormatException e) {
            throw new IllegalStateException("NAB length prefix is invalid: " + prefix
                    + " (expected " + lengthBytes + " digits)", e);
        }
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
