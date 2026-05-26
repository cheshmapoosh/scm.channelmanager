package ir.daneshrefah.scm.provider.nab.tcp;

import ir.daneshrefah.scm.provider.nab.config.NabResolvedConfig;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NabConnectionPoolRegistry implements AutoCloseable {
    private final Clock clock;
    private final Map<String, NabConnectionPool> pools = new ConcurrentHashMap<>();

    public NabConnectionPoolRegistry() {
        this(Clock.systemUTC());
    }

    NabConnectionPoolRegistry(Clock clock) {
        this.clock = clock;
    }

    NabConnectionPool pool(NabResolvedConfig config) {
        return pools.computeIfAbsent(key(config), ignored -> new NabConnectionPool(config, clock));
    }

    private String key(NabResolvedConfig config) {
        return config.provider()
                + "|" + config.endpoints()
                + "|" + config.charset()
                + "|" + config.connectionPool();
    }

    @PreDestroy
    public void close() {
        pools.values().forEach(NabConnectionPool::close);
        pools.clear();
    }
}
