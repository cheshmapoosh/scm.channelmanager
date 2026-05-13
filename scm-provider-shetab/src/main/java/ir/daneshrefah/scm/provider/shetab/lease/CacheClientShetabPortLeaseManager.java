package ir.daneshrefah.scm.provider.shetab.lease;

import ir.daneshrefah.scm.cache.client.utility.resourcelease.ResourceLease;
import ir.daneshrefah.scm.cache.client.utility.resourcelease.ResourceLeaseUtility;
import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class CacheClientShetabPortLeaseManager implements ShetabPortLeaseManager {

    private static final String DEFAULT_LOCAL_ADDRESS = "0.0.0.0";
    private static final String POOL_PREFIX = "shetab-local-port";

    private final ResourceLeaseUtility resourceLeaseUtility;

    @Override
    public ShetabPortLease acquire(ShetabResolvedConfig config) {
        if (config.portLease() == null || !config.portLease().enabled()) {
            return ShetabPortLease.none();
        }
        List<String> portCandidates = toPortCandidates(config.localPorts());
        if (portCandidates.isEmpty()) {
            return ShetabPortLease.none();
        }

        String localAddress = normalizeLocalAddress(config.localAddress());
        String poolName = POOL_PREFIX + "::" + localAddress;
        Duration ttl = Duration.ofMillis(Math.max(1_000L, config.portLease().ttlMs()));

        ResourceLease lease = resourceLeaseUtility.acquire(poolName, portCandidates, ttl);
        int selectedPort = parsePortOrRelease(lease, config.provider());

        log.info("Acquired Shetab local port lease via cache-client utility: provider={}, localAddress={}, port={}",
                config.provider(), localAddress, selectedPort);

        return new ResourceLeaseBackedShetabPortLease(lease, selectedPort, config.provider(), localAddress);
    }

    private List<String> toPortCandidates(List<Integer> ports) {
        if (ports == null || ports.isEmpty()) {
            return List.of();
        }
        List<String> candidates = new ArrayList<>(ports.size());
        for (Integer port : ports) {
            if (port != null && port > 0) {
                candidates.add(String.valueOf(port));
            }
        }
        return candidates;
    }

    private String normalizeLocalAddress(String localAddress) {
        if (!StringUtils.hasText(localAddress)) {
            return DEFAULT_LOCAL_ADDRESS;
        }
        return localAddress.trim();
    }

    private int parsePortOrRelease(ResourceLease lease, String providerName) {
        try {
            return Integer.parseInt(lease.resourceName());
        } catch (NumberFormatException exception) {
            lease.close();
            throw new IllegalStateException(
                    "Could not parse leased port for provider='" + providerName + "', resource='" + lease.resourceName() + "'",
                    exception);
        }
    }

    private static final class ResourceLeaseBackedShetabPortLease implements ShetabPortLease {
        private final ResourceLease delegate;
        private final int port;
        private final String providerName;
        private final String localAddress;

        private ResourceLeaseBackedShetabPortLease(ResourceLease delegate, int port, String providerName, String localAddress) {
            this.delegate = delegate;
            this.port = port;
            this.providerName = providerName;
            this.localAddress = localAddress;
        }

        @Override
        public int port() {
            return port;
        }

        @Override
        public void close() {
            delegate.close();
            log.info("Released Shetab local port lease via cache-client utility: provider={}, localAddress={}, port={}",
                    providerName, localAddress, port);
        }
    }
}
