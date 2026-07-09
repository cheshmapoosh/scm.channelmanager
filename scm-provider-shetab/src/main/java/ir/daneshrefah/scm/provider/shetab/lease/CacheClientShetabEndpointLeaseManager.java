package ir.daneshrefah.scm.provider.shetab.lease;

import ir.daneshrefah.scm.cache.starter.utility.resourcelease.ResourceLease;
import ir.daneshrefah.scm.cache.starter.utility.resourcelease.ResourceLeaseUtility;
import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class CacheClientShetabEndpointLeaseManager implements ShetabEndpointLeaseManager {

    private static final String POOL_PREFIX = "shetab-endpoint-lease";

    private final ResourceLeaseUtility resourceLeaseUtility;

    @Override
    public ShetabEndpointLease acquire(ShetabResolvedConfig config) {
        List<String> endpointCandidates = toEndpointCandidates(config.endpoints());
        if (endpointCandidates.isEmpty()) {
            return ShetabEndpointLease.none();
        }
        if (config.endpointLease() == null || !config.endpointLease().enabled()) {
            EndpointParts endpoint = parseEndpoint(endpointCandidates.getFirst(), config.provider());
            log.info("Using Shetab HPS endpoint without distributed lease: provider={}, endpoint={}",
                    config.provider(), endpoint.rawEndpoint());
            return new StaticShetabEndpointLease(endpoint.rawEndpoint(), endpoint.host(), endpoint.port());
        }

        String poolName = POOL_PREFIX + "::" + config.provider();
        Duration ttl = Duration.ofMillis(Math.max(1_000L, config.endpointLease().ttlMs()));

        ResourceLease lease = resourceLeaseUtility.acquire(poolName, endpointCandidates, ttl);
        EndpointParts leasedEndpoint = parseEndpointOrRelease(lease, config.provider());

        log.info("Acquired Shetab HPS endpoint lease via cache-client utility: provider={}, endpoint={}",
                config.provider(), leasedEndpoint.rawEndpoint());

        return new ResourceLeaseBackedShetabEndpointLease(lease, leasedEndpoint.rawEndpoint(), leasedEndpoint.host(), leasedEndpoint.port(), config.provider());
    }

    private List<String> toEndpointCandidates(List<String> endpoints) {
        if (endpoints == null || endpoints.isEmpty()) {
            return List.of();
        }
        return endpoints.stream()
                .filter(endpoint -> endpoint != null && !endpoint.isBlank())
                .map(String::trim)
                .toList();
    }

    private EndpointParts parseEndpointOrRelease(ResourceLease lease, String providerName) {
        try {
            return parseEndpoint(lease.resourceName(), providerName);
        } catch (Exception exception) {
            lease.close();
            throw new IllegalStateException(
                    "Could not parse leased endpoint for provider='" + providerName + "', resource='" + lease.resourceName() + "'",
                    exception);
        }
    }

    private EndpointParts parseEndpoint(String endpoint, String providerName) {
        int separator = endpoint.lastIndexOf(':');
        if (separator <= 0 || separator == endpoint.length() - 1) {
            throw new IllegalStateException("Invalid endpoint for provider '" + providerName + "': " + endpoint);
        }
        String host = endpoint.substring(0, separator).trim();
        int port = Integer.parseInt(endpoint.substring(separator + 1).trim());
        if (host.isBlank() || port < 1) {
            throw new IllegalStateException("Invalid endpoint for provider '" + providerName + "': " + endpoint);
        }
        return new EndpointParts(endpoint, host, port);
    }

    private static final class ResourceLeaseBackedShetabEndpointLease implements ShetabEndpointLease {
        private final ResourceLease delegate;
        private final String endpoint;
        private final String remoteHost;
        private final int remotePort;
        private final String providerName;

        private ResourceLeaseBackedShetabEndpointLease(
                ResourceLease delegate,
                String endpoint,
                String remoteHost,
                int remotePort,
                String providerName
        ) {
            this.delegate = delegate;
            this.endpoint = endpoint;
            this.remoteHost = remoteHost;
            this.remotePort = remotePort;
            this.providerName = providerName;
        }

        @Override
        public String endpoint() {
            return endpoint;
        }

        @Override
        public String remoteHost() {
            return remoteHost;
        }

        @Override
        public int remotePort() {
            return remotePort;
        }

        @Override
        public void close() {
            delegate.close();
            log.info("Released Shetab HPS endpoint lease via cache-client utility: provider={}, endpoint={}",
                    providerName, endpoint);
        }
    }

    private static final class StaticShetabEndpointLease implements ShetabEndpointLease {
        private final String endpoint;
        private final String remoteHost;
        private final int remotePort;

        private StaticShetabEndpointLease(String endpoint, String remoteHost, int remotePort) {
            this.endpoint = endpoint;
            this.remoteHost = remoteHost;
            this.remotePort = remotePort;
        }

        @Override
        public String endpoint() {
            return endpoint;
        }

        @Override
        public String remoteHost() {
            return remoteHost;
        }

        @Override
        public int remotePort() {
            return remotePort;
        }

        @Override
        public void close() {
        }
    }

    private record EndpointParts(String rawEndpoint, String host, int port) {
    }
}
