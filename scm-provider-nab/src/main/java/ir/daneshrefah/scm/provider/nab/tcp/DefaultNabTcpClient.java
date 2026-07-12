package ir.daneshrefah.scm.provider.nab.tcp;

import ir.daneshrefah.scm.provider.nab.codec.NabTextNormalizer;
import ir.daneshrefah.scm.provider.nab.config.NabResolvedConfig;
import ir.daneshrefah.scm.provider.nab.domain.NabWireRequest;
import ir.daneshrefah.scm.provider.nab.metrics.NabProviderMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultNabTcpClient implements NabTcpClient {
    private final NabTextNormalizer textNormalizer;
    private final NabProviderMetrics metrics;

    @Override
    public String request(NabResolvedConfig config, NabWireRequest request) {
        String protocol = textNormalizer.normalize(request.protocol(), config);
        String body = textNormalizer.normalize(request.bodyWithoutProtocol(), config);
        return requestSingleUse(config, protocol, body);
    }

    private String requestSingleUse(NabResolvedConfig config, String protocol, String body) {
        NabProviderMetrics.CounterSet providerMetrics = metrics.provider(config.provider());
        NabEndpointAddress endpoint = NabEndpointAddress.parse(config.endpoint());
        providerMetrics.connectionOpened();
        try (NabTcpSession connection = NabTcpSession.connect(
                endpoint,
                config,
                Charset.forName(config.charset())
        )) {
            logWire(config, "sent-protocol", protocol);
            logWire(config, "sent-body", body);
            String response = connection.request(config, protocol, body);
            response = textNormalizer.normalize(response, config);
            logWire(config, "received-response", response);
            log.info("NAB request completed provider={} endpoint={} responseChars={} protocol={}",
                    config.provider(), endpoint.value(), response.length(), protocol);
            return response;
        } catch (RuntimeException e) {
            if (containsIgnoreCase(e.getMessage(), "acknowledge")) {
                providerMetrics.ackFailed();
            }
            log.error("NAB request failed provider={} endpoint={} protocol={} failureType={}",
                    config.provider(), endpoint.value(), protocol, e.getClass().getSimpleName());
            throw e;
        } finally {
            providerMetrics.connectionClosed();
        }
    }

    private void logWire(NabResolvedConfig config, String direction, String content) {
        log.debug("NAB wire provider={} direction={} chars={}", config.provider(), direction, content.length());
    }

    private boolean containsIgnoreCase(String value, String token) {
        if (value == null || token == null) {
            return false;
        }
        return value.toLowerCase().contains(token.toLowerCase());
    }
}
