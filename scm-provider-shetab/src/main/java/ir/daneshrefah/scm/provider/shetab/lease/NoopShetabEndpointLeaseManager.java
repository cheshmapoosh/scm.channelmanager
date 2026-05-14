package ir.daneshrefah.scm.provider.shetab.lease;

import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;

import java.util.List;

public class NoopShetabEndpointLeaseManager implements ShetabEndpointLeaseManager {
    @Override
    public ShetabEndpointLease acquire(ShetabResolvedConfig config) {
        List<String> endpoints = config.endpoints();
        if (endpoints == null || endpoints.isEmpty()) {
            return ShetabEndpointLease.none();
        }
        EndpointParts endpointParts = parseEndpoint(endpoints.get(0), config.provider());
        return new SimpleShetabEndpointLease(endpointParts.rawEndpoint(), endpointParts.host(), endpointParts.port());
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

    private record EndpointParts(String rawEndpoint, String host, int port) {
    }

    private record SimpleShetabEndpointLease(String endpoint, String remoteHost, int remotePort) implements ShetabEndpointLease {
        @Override
        public void close() {
        }
    }
}
