package ir.daneshrefah.scm.provider.shetab.tcp;

import ir.daneshrefah.scm.common.provider.readiness.ProviderReadinessContributor;
import ir.daneshrefah.scm.common.provider.runtime.ProviderRuntimeLifecycle;
import ir.daneshrefah.scm.provider.shetab.config.ShetabConfigResolver;
import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import ir.daneshrefah.scm.provider.shetab.iso.ShetabPackagerFactory;
import ir.daneshrefah.scm.provider.shetab.lease.ShetabEndpointLeaseManager;
import ir.daneshrefah.scm.provider.shetab.metrics.ShetabProviderMetrics;
import ir.daneshrefah.scm.provider.shetab.trace.ShetabProviderTraceLifecycle;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.jpos.iso.ISOMsg;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class ShetabTcpClientRegistry implements
        ShetabClientRegistry,
        ProviderRuntimeLifecycle,
        ProviderReadinessContributor {

    private final ShetabPackagerFactory packagerFactory;
    private final ShetabEndpointLeaseManager endpointLeaseManager;
    private final ShetabProviderMetrics metrics;
    private final ShetabConfigResolver configResolver;
    private final Map<String, RegisteredClient> clients = new ConcurrentHashMap<>();
    private final AtomicBoolean applicationReady = new AtomicBoolean(false);
    private final AtomicBoolean registrationCompleted = new AtomicBoolean(false);

    public ShetabTcpClientRegistry(
            ShetabPackagerFactory packagerFactory,
            ShetabEndpointLeaseManager endpointLeaseManager,
            ShetabProviderMetrics metrics,
            ShetabConfigResolver configResolver
    ) {
        this.packagerFactory = packagerFactory;
        this.endpointLeaseManager = endpointLeaseManager;
        this.metrics = metrics;
        this.configResolver = configResolver;
    }

    @Override
    public boolean supports(String scheme) {
        return ShetabConfigResolver.COMPONENT_SCHEME.equalsIgnoreCase(scheme);
    }

    @Override
    public void registerEffectiveUsage(EffectiveProviderUsage usage) {
        Objects.requireNonNull(usage, "usage");
        if (!supports(usage.scheme())) {
            return;
        }

        ShetabResolvedConfig config;
        try {
            config = configResolver.resolve(usage.providerUri(), null);
        } catch (RuntimeException failure) {
            throw new IllegalStateException(
                    "Invalid effective Shetab provider configuration: service='" + usage.serviceCode()
                            + "' operation='" + usage.operationName()
                            + "' provider='" + usage.providerCode() + "'",
                    failure
            );
        }

        String providerKey = normalizeProviderKey(config.provider());
        RuntimeConfigSignature requestedSignature = RuntimeConfigSignature.from(providerKey, config);
        RegisteredClient registered = clients.computeIfAbsent(
                providerKey,
                ignored -> createClient(config, requestedSignature)
        );
        registered.verifyCompatible(requestedSignature, config.provider());
        registered.addUsage(usage);
        if (applicationReady.get()) {
            registered.client().start();
        }
    }

    @Override
    public void registrationComplete() {
        if (!registrationCompleted.compareAndSet(false, true)) {
            return;
        }
        for (ShetabConfigResolver.ConfiguredShetabProvider configured : configResolver.configuredProviders()) {
            if (configured.enabled() && !clients.containsKey(normalizeProviderKey(configured.providerCode()))) {
                log.warn("event=SHETAB_PROVIDER_ENABLED_BUT_UNUSED provider={} outcome=ignored",
                        configured.providerCode());
            }
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void applicationReady() {
        if (!applicationReady.compareAndSet(false, true)) {
            return;
        }
        clients.values().forEach(registered -> registered.client().start());
    }

    @Override
    public ShetabTransportResponse request(
            ShetabResolvedConfig config,
            ISOMsg request,
            ShetabProviderTraceLifecycle traceLifecycle
    ) {
        Objects.requireNonNull(traceLifecycle, "traceLifecycle");
        String providerKey = normalizeProviderKey(config.provider());
        RegisteredClient registered = clients.get(providerKey);
        if (registered == null) {
            throw new ShetabConnectionUnavailableException(
                    "reasonCode=SHETAB_CONNECTION_UNAVAILABLE Shetab runtime is not registered provider="
                            + config.provider());
        }

        registered.verifyCompatible(RuntimeConfigSignature.from(providerKey, config), config.provider());
        return registered.client().request(request, config.responseTimeoutMs(), traceLifecycle);
    }

    @Override
    public List<ProviderReadiness> readinessSnapshots() {
        return clients.values().stream()
                .sorted((left, right) -> String.CASE_INSENSITIVE_ORDER.compare(
                        left.signature().providerKey(), right.signature().providerKey()))
                .map(registered -> toReadiness(registered.client().snapshot()))
                .toList();
    }

    @PreDestroy
    public void stop() {
        applicationReady.set(false);
        clients.values().forEach(registered -> registered.client().stop());
    }

    private RegisteredClient createClient(ShetabResolvedConfig config, RuntimeConfigSignature signature) {
        ShetabIsoChannelClient client = new ShetabIsoChannelClient(
                config,
                packagerFactory,
                endpointLeaseManager,
                metrics
        );
        return new RegisteredClient(client, signature);
    }

    private ProviderReadiness toReadiness(ShetabConnectionSnapshot snapshot) {
        Map<String, Object> details = new LinkedHashMap<>();
        put(details, "provider", snapshot.providerCode());
        put(details, "connectionState", snapshot.connectionState().name());
        details.put("verified", snapshot.verified());
        put(details, "endpoint", snapshot.endpoint());
        details.put("generation", snapshot.generation());
        put(details, "leaseState", snapshot.leaseState().name());
        details.put("reconnectAttempt", snapshot.reconnectAttempt());
        details.put("recoveryInProgress", snapshot.recoveryInProgress());
        details.put("recoveryRequired", snapshot.recoveryRequired());
        details.put("connectionWorkerAlive", snapshot.connectionWorkerAlive());
        put(details, "lastStateChangedAt", snapshot.lastStateChangedAt());
        put(details, "lastConnectedAt", snapshot.lastConnectedAt());
        put(details, "lastDisconnectedAt", snapshot.lastDisconnectedAt());
        put(details, "lastValidatedAt", snapshot.lastValidatedAt());
        put(details, "lastFailureAt", snapshot.lastFailureAt());
        put(details, "lastFailurePhase", snapshot.lastFailurePhase());
        put(details, "lastFailureCode", snapshot.lastFailureCode());
        put(details, "lastFailureType", snapshot.lastFailureType());
        put(details, "lastFailureMessage", snapshot.lastFailureMessage());
        put(details, "nextRetryDelayMs", snapshot.nextRetryDelayMs());
        return new ProviderReadiness(snapshot.providerCode(), snapshot.ready(), details);
    }

    private void put(Map<String, Object> details, String key, Object value) {
        if (value != null) {
            details.put(key, value instanceof Instant instant ? instant.toString() : value);
        }
    }

    private String normalizeProviderKey(String provider) {
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("Shetab provider is required");
        }
        return provider.trim().toLowerCase(Locale.ROOT);
    }

    private static final class RegisteredClient {
        private final ShetabIsoChannelClient client;
        private final RuntimeConfigSignature signature;
        private final List<EffectiveProviderUsage> usages = new ArrayList<>();

        private RegisteredClient(ShetabIsoChannelClient client, RuntimeConfigSignature signature) {
            this.client = client;
            this.signature = signature;
        }

        ShetabIsoChannelClient client() {
            return client;
        }

        RuntimeConfigSignature signature() {
            return signature;
        }

        synchronized void addUsage(EffectiveProviderUsage usage) {
            if (!usages.contains(usage)) {
                usages.add(usage);
            }
        }

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
