package ir.daneshrefah.scm.provider.shetab.tcp;

import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import ir.daneshrefah.scm.provider.shetab.iso.ShetabPackagerFactory;
import ir.daneshrefah.scm.provider.shetab.lease.ShetabEndpointLeaseManager;
import ir.daneshrefah.scm.provider.shetab.metrics.ShetabProviderMetrics;
import ir.daneshrefah.scm.provider.shetab.trace.ShetabProviderTraceLifecycle;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ShetabTcpClientRegistry implements ShetabClientRegistry {
    private final ShetabPackagerFactory packagerFactory;
    private final ShetabEndpointLeaseManager endpointLeaseManager;
    private final ShetabProviderMetrics metrics;
    private final Map<String, RegisteredClient> clients = new ConcurrentHashMap<>();

    @Override
    public ShetabTransportResponse request(
            ShetabResolvedConfig config,
            ISOMsg request,
            ShetabProviderTraceLifecycle traceLifecycle
    ) {
        Objects.requireNonNull(traceLifecycle, "traceLifecycle");
        String providerKey = normalizeProviderKey(config.provider());
        RuntimeConfigSignature requestedSignature = RuntimeConfigSignature.from(providerKey, config);
        RegisteredClient registeredClient = clients.computeIfAbsent(
                providerKey,
                ignored -> createClient(config, requestedSignature)
        );

        registeredClient.verifyCompatible(requestedSignature, config.provider());

        return registeredClient.client().request(
                request,
                config.responseTimeoutMs(),
                traceLifecycle
        );
    }

    private RegisteredClient createClient(ShetabResolvedConfig config, RuntimeConfigSignature signature) {
        ShetabIsoChannelClient client = new ShetabIsoChannelClient(config, packagerFactory, endpointLeaseManager, metrics);
        client.start();
        return new RegisteredClient(client, signature);
    }

    @PreDestroy
    public void stop() {
        clients.values().forEach(registeredClient -> registeredClient.client().stop());
        clients.clear();
    }

    private String normalizeProviderKey(String provider) {
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("Shetab provider is required");
        }

        return provider.trim().toLowerCase(Locale.ROOT);
    }

    private record RegisteredClient(
            ShetabIsoChannelClient client,
            RuntimeConfigSignature signature
    ) {
        void verifyCompatible(RuntimeConfigSignature requestedSignature, String requestedProvider) {
            if (signature.equals(requestedSignature)) {
                return;
            }

            throw new IllegalStateException(
                    "Incompatible Shetab TCP client runtime config for provider=" + requestedProvider
                            + " normalizedProvider=" + requestedSignature.providerKey()
                            + " changedFields=" + signature.differences(requestedSignature)
            );
        }
    }

    private record RuntimeConfigSignature(
            String providerKey,
            String scheme,
            List<String> endpoints,
            String packagerClass,
            String packagerXml,
            int connectTimeoutMs,
            int socketTimeoutMs,
            boolean keepAlive,
            int sendTimeoutMs,
            int reconnectDelayMs,
            int sameEndpointReconnectAttempts,
            int queueCapacity,
            boolean endpointLeaseEnabled,
            long endpointLeaseTtlMs
    ) {
        static RuntimeConfigSignature from(String providerKey, ShetabResolvedConfig config) {
            ShetabResolvedConfig.EndpointLease endpointLease = config.endpointLease();

            return new RuntimeConfigSignature(
                    providerKey,
                    normalize(config.scheme()).toLowerCase(Locale.ROOT),
                    normalizeEndpoints(config.endpoints()),
                    normalize(config.packagerClass()),
                    normalize(config.packagerXml()),
                    config.connectTimeoutMs(),
                    config.socketTimeoutMs(),
                    config.keepAlive(),
                    config.sendTimeoutMs(),
                    config.reconnectDelayMs(),
                    config.sameEndpointReconnectAttempts(),
                    config.queueCapacity(),
                    endpointLease != null && endpointLease.enabled(),
                    endpointLease == null ? 0L : endpointLease.ttlMs()
            );
        }

        List<String> differences(RuntimeConfigSignature other) {
            List<String> differences = new ArrayList<>();

            addDifference(differences, "providerKey", providerKey, other.providerKey);
            addDifference(differences, "scheme", scheme, other.scheme);
            addDifference(differences, "endpoints", endpoints, other.endpoints);
            addDifference(differences, "packagerClass", packagerClass, other.packagerClass);
            addDifference(differences, "packagerXml", packagerXml, other.packagerXml);
            addDifference(differences, "connectTimeoutMs", connectTimeoutMs, other.connectTimeoutMs);
            addDifference(differences, "socketTimeoutMs", socketTimeoutMs, other.socketTimeoutMs);
            addDifference(differences, "keepAlive", keepAlive, other.keepAlive);
            addDifference(differences, "sendTimeoutMs", sendTimeoutMs, other.sendTimeoutMs);
            addDifference(differences, "reconnectDelayMs", reconnectDelayMs, other.reconnectDelayMs);
            addDifference(differences, "sameEndpointReconnectAttempts", sameEndpointReconnectAttempts,
                    other.sameEndpointReconnectAttempts);
            addDifference(differences, "queueCapacity", queueCapacity, other.queueCapacity);
            addDifference(differences, "endpointLeaseEnabled", endpointLeaseEnabled, other.endpointLeaseEnabled);
            addDifference(differences, "endpointLeaseTtlMs", endpointLeaseTtlMs, other.endpointLeaseTtlMs);

            return List.copyOf(differences);
        }

        private static List<String> normalizeEndpoints(List<String> endpoints) {
            if (endpoints == null || endpoints.isEmpty()) {
                return List.of();
            }

            return endpoints.stream()
                    .map(RuntimeConfigSignature::normalize)
                    .filter(value -> !value.isBlank())
                    .toList();
        }

        private static String normalize(String value) {
            return value == null ? "" : value.trim();
        }

        private static void addDifference(List<String> differences, String field, Object left, Object right) {
            if (!Objects.equals(left, right)) {
                differences.add(field);
            }
        }
    }
}
