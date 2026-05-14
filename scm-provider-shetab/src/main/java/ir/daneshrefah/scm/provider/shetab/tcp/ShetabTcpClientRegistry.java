package ir.daneshrefah.scm.provider.shetab.tcp;

import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import ir.daneshrefah.scm.provider.shetab.iso.ShetabPackagerFactory;
import ir.daneshrefah.scm.provider.shetab.lease.ShetabEndpointLeaseManager;
import ir.daneshrefah.scm.provider.shetab.metrics.ShetabProviderMetrics;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ShetabTcpClientRegistry implements ShetabClientRegistry {
    private final ShetabPackagerFactory packagerFactory;
    private final ShetabEndpointLeaseManager endpointLeaseManager;
    private final ShetabProviderMetrics metrics;
    private final Map<String, ShetabIsoChannelClient> clients = new ConcurrentHashMap<>();

    @Override
    public ISOMsg request(ShetabResolvedConfig config, ISOMsg request) {
        return clients.computeIfAbsent(config.provider(), ignored -> createClient(config)).request(request, config.responseTimeoutMs());
    }

    private ShetabIsoChannelClient createClient(ShetabResolvedConfig config) {
        ShetabIsoChannelClient client = new ShetabIsoChannelClient(config, packagerFactory, endpointLeaseManager, metrics);
        client.start();
        return client;
    }

    @PreDestroy
    public void stop() {
        clients.values().forEach(ShetabIsoChannelClient::stop);
        clients.clear();
    }
}
