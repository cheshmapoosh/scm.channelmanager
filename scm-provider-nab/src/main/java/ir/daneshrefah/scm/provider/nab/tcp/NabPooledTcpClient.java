package ir.daneshrefah.scm.provider.nab.tcp;

import ir.daneshrefah.scm.provider.nab.codec.NabTextNormalizer;
import ir.daneshrefah.scm.provider.nab.config.NabResolvedConfig;
import ir.daneshrefah.scm.provider.nab.domain.NabWireRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.time.Clock;

@Slf4j
@Component
@RequiredArgsConstructor
public class NabPooledTcpClient implements NabTcpClient {
    private final NabTextNormalizer textNormalizer;
    private final NabConnectionPoolRegistry poolRegistry;

    @Override
    public String request(NabResolvedConfig config, NabWireRequest request) {
        String protocol = textNormalizer.normalize(request.protocol(), config);
        String body = textNormalizer.normalize(request.bodyWithoutProtocol(), config);
        if (!config.connectionPool().enabled()) {
            return requestWithoutPool(config, protocol, body);
        }

        NabConnectionPool pool = poolRegistry.pool(config);
        NabPooledConnection connection = pool.borrow();
        boolean reusable = false;
        try {
            logWire(config, "sent-protocol", protocol);
            logWire(config, "sent-body", body);
            String response = connection.request(config, protocol, body);
            response = textNormalizer.normalize(response, config);
            logWire(config, "received-response", response);
            log.info("NAB pooled request completed provider={} endpoint={} responseChars={}",
                    config.provider(), connection.endpointValue(), response.length());
            reusable = true;
            return response;
        } catch (RuntimeException e) {
            log.error("NAB pooled request failed provider={} endpoint={}",
                    config.provider(), connection.endpointValue(), e);
            throw e;
        } finally {
            pool.release(connection, reusable);
        }
    }

    private String requestWithoutPool(NabResolvedConfig config, String protocol, String body) {
        NabEndpointAddress endpoint = NabEndpointAddress.parse(config.endpoints().getFirst());
        NabPooledConnection connection = NabPooledConnection.open(
                endpoint,
                config,
                Charset.forName(config.charset()),
                Clock.systemUTC()
        );
        try {
            logWire(config, "sent-protocol", protocol);
            logWire(config, "sent-body", body);
            String response = connection.request(config, protocol, body);
            response = textNormalizer.normalize(response, config);
            logWire(config, "received-response", response);
            return response;
        } finally {
            connection.close();
        }
    }

    private void logWire(NabResolvedConfig config, String direction, String content) {
        if (config.wireLogEnabled()) {
            log.info("NAB wire provider={} direction={} content=[{}]", config.provider(), direction, content);
        } else {
            log.debug("NAB wire provider={} direction={} chars={}", config.provider(), direction, content.length());
        }
    }
}
