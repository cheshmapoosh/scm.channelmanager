package ir.daneshrefah.scm.cache.client.connector.backend;

import ir.daneshrefah.scm.cache.client.config.properties.CacheType;
import lombok.extern.slf4j.Slf4j;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CacheBackendRouter {

    private final Map<CacheType, CacheBackend> backendMap = new EnumMap<>(CacheType.class);

    public CacheBackendRouter(List<CacheBackend> backends) {
        for (CacheBackend backend : backends) {
            CacheBackend previous = backendMap.put(backend.type(), backend);
            if (previous != null) {
                log.warn("Cache backend for type '{}' was overridden by '{}'", backend.type(), backend.getClass().getName());
            }
            log.info("Cache backend registered: {}", backend.type());
        }
    }

    public CacheBackend get(CacheType type) {
        CacheBackend backend = backendMap.get(type);
        if (backend == null) {
            throw new IllegalStateException("Cache backend is not configured for type: " + type);
        }
        return backend;
    }
}
