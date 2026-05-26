package ir.daneshrefah.scm.provider.nab.tcp;

import ir.daneshrefah.scm.provider.nab.config.NabResolvedConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.nio.charset.Charset;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
final class NabConnectionPool implements AutoCloseable {
    private final NabResolvedConfig config;
    private final GenericObjectPool<NabPooledConnection> delegate;

    NabConnectionPool(NabResolvedConfig config, Clock clock) {
        this.config = config;
        this.delegate = new GenericObjectPool<>(
                new NabConnectionFactory(config, Charset.forName(config.charset()), clock),
                poolConfig(config)
        );
        if (config.connectionPool().prefill()) {
            prefill();
        }
        log.info("Created NAB Commons Pool2 connection pool provider={} maxTotal={} minIdle={} maxIdle={} endpoints={}",
                config.provider(), config.connectionPool().maxTotal(), config.connectionPool().minIdle(),
                config.connectionPool().maxIdle(), config.endpoints());
    }

    NabPooledConnection borrow() {
        try {
            NabPooledConnection connection = delegate.borrowObject();
            log.debug("Borrowed NAB pooled connection provider={} endpoint={} active={} idle={}",
                    config.provider(), connection.endpointValue(), delegate.getNumActive(), delegate.getNumIdle());
            return connection;
        } catch (Exception e) {
            throw new IllegalStateException("Could not borrow NAB connection provider=" + config.provider()
                    + " maxWaitMs=" + config.connectionPool().maxWaitMs(), e);
        }
    }

    void release(NabPooledConnection connection, boolean reusable) {
        if (connection == null) {
            return;
        }
        if (!reusable || connection.stale(config)) {
            invalidate(connection, reusable ? "stale-on-release" : "not-reusable");
            return;
        }
        try {
            delegate.returnObject(connection);
            log.debug("Returned NAB pooled connection provider={} endpoint={} active={} idle={}",
                    config.provider(), connection.endpointValue(), delegate.getNumActive(), delegate.getNumIdle());
        } catch (Exception e) {
            log.warn("Could not return NAB connection to pool provider={} endpoint={}",
                    config.provider(), connection.endpointValue(), e);
            invalidate(connection, "return-failed");
        }
    }

    private void invalidate(NabPooledConnection connection, String reason) {
        try {
            delegate.invalidateObject(connection);
            log.debug("Invalidated NAB pooled connection provider={} endpoint={} reason={} active={} idle={}",
                    config.provider(), connection.endpointValue(), reason, delegate.getNumActive(), delegate.getNumIdle());
        } catch (Exception e) {
            log.warn("Could not invalidate NAB pooled connection provider={} endpoint={} reason={}",
                    config.provider(), connection.endpointValue(), reason, e);
            connection.close();
        }
    }

    private void prefill() {
        try {
            delegate.addObjects(config.connectionPool().minIdle());
        } catch (Exception e) {
            log.warn("Could not prefill NAB connection pool provider={}", config.provider(), e);
        }
    }

    private GenericObjectPoolConfig<NabPooledConnection> poolConfig(NabResolvedConfig config) {
        GenericObjectPoolConfig<NabPooledConnection> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(config.connectionPool().maxTotal());
        poolConfig.setMaxIdle(config.connectionPool().maxIdle());
        poolConfig.setMinIdle(config.connectionPool().minIdle());
        poolConfig.setMaxWait(Duration.ofMillis(config.connectionPool().maxWaitMs()));
        poolConfig.setTestOnBorrow(config.connectionPool().testOnBorrow());
        poolConfig.setTestOnReturn(config.connectionPool().testOnReturn());
        poolConfig.setTestWhileIdle(config.connectionPool().testWhileIdle());
        poolConfig.setBlockWhenExhausted(config.connectionPool().blockWhenExhausted());
        poolConfig.setLifo(config.connectionPool().lifo());
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(config.connectionPool().timeBetweenEvictionRunsMs()));
        poolConfig.setMinEvictableIdleDuration(Duration.ofMillis(config.connectionPool().minEvictableIdleTimeMs()));
        poolConfig.setSoftMinEvictableIdleDuration(Duration.ofMillis(config.connectionPool().softMinEvictableIdleTimeMs()));
        return poolConfig;
    }

    @Override
    public void close() {
        delegate.close();
        log.info("Closed NAB Commons Pool2 connection pool provider={}", config.provider());
    }

    private static final class NabConnectionFactory extends BasePooledObjectFactory<NabPooledConnection> {
        private final NabResolvedConfig config;
        private final Charset charset;
        private final Clock clock;
        private final List<NabEndpointAddress> endpoints;
        private final AtomicInteger endpointIndex = new AtomicInteger();

        private NabConnectionFactory(NabResolvedConfig config, Charset charset, Clock clock) {
            this.config = config;
            this.charset = charset;
            this.clock = clock;
            this.endpoints = config.endpoints().stream().map(NabEndpointAddress::parse).toList();
        }

        @Override
        public NabPooledConnection create() {
            return NabPooledConnection.open(nextEndpoint(), config, charset, clock);
        }

        @Override
        public PooledObject<NabPooledConnection> wrap(NabPooledConnection connection) {
            return new DefaultPooledObject<>(connection);
        }

        @Override
        public boolean validateObject(PooledObject<NabPooledConnection> pooledObject) {
            NabPooledConnection connection = pooledObject.getObject();
            boolean valid = connection.reusable(config);
            if (!valid) {
                log.debug("NAB pooled connection is not valid provider={} endpoint={} ageMs={} idleForMs={}",
                        config.provider(), connection.endpointValue(), connection.ageMs(), connection.idleForMs());
            }
            return valid;
        }

        @Override
        public void destroyObject(PooledObject<NabPooledConnection> pooledObject) {
            pooledObject.getObject().close();
        }

        private NabEndpointAddress nextEndpoint() {
            int index = Math.floorMod(endpointIndex.getAndIncrement(), endpoints.size());
            return endpoints.get(index);
        }
    }
}
