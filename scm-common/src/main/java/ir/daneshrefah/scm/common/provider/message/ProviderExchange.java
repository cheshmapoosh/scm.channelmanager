package ir.daneshrefah.scm.common.provider.message;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ProviderExchange {
    private final ProviderRequest request;
    private ProviderResponse response;
    private final ProviderMessageCustomizerContext context;
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    public ProviderExchange(ProviderRequest request, ProviderMessageCustomizerContext context) {
        if (request == null) {
            throw new IllegalArgumentException("Provider request is required");
        }
        if (context == null) {
            throw new IllegalArgumentException("Provider customizer context is required");
        }
        this.request = request;
        this.context = context;
    }

    public ProviderRequest request() {
        return request;
    }

    public ProviderResponse response() {
        return response;
    }

    public void response(ProviderResponse response) {
        this.response = response;
    }

    public ProviderMessageCustomizerContext context() {
        return context;
    }

    public Map<String, Object> attributes() {
        return attributes;
    }

    public void putAttribute(String key, Object value) {
        if (key == null || key.isBlank()) {
            return;
        }
        if (value == null) {
            attributes.remove(key);
            return;
        }
        attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        return attributes.get(key);
    }

    public <T> T getAttribute(String key, Class<T> type) {
        return findAttribute(key, type).orElse(null);
    }

    public <T> Optional<T> findAttribute(String key, Class<T> type) {
        if (type == null) {
            return Optional.empty();
        }
        Object value = getAttribute(key);
        if (value == null || !type.isInstance(value)) {
            return Optional.empty();
        }
        return Optional.of(type.cast(value));
    }
}
