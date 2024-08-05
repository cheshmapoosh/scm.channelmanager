package ir.daneshrefah.scm.common.data.audit.util;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class AuditableTypeCache {

    private static final Map<Object, Object> CACHE_MAP = new ConcurrentHashMap<>();
    private static final AuditableTypeCache SIMPLE_CACHE = new AuditableTypeCache();

    private AuditableTypeCache() {
    }

    public static AuditableTypeCache getInstance() {
        return SIMPLE_CACHE;
    }

    public void put(Object key, Object value) {
        CACHE_MAP.put(key, value);
    }

    public Optional<Object> find(Object key) {
        if (CACHE_MAP.containsKey(key)) {
            return Optional.of(CACHE_MAP.get(key));
        }
        return Optional.empty();
    }
}
