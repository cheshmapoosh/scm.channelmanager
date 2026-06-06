package ir.daneshrefah.scm.common.provider.message;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProviderRequest {
    private String method;
    private URI uri;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private final Map<String, Object> queryParameters = new LinkedHashMap<>();
    private final Map<String, Object> fields = new LinkedHashMap<>();
    private Object body;
    private Object nativeRequest;

    public ProviderRequest() {
    }

    public ProviderRequest(String method, URI uri, Map<String, String> headers, Object body) {
        this.method = method;
        this.uri = uri;
        if (headers != null) {
            this.headers.putAll(headers);
        }
        this.body = mutableBody(body);
        copyBodyFields(this.body);
    }

    public String method() {
        return method;
    }

    public void method(String method) {
        this.method = method;
    }

    public URI uri() {
        return uri;
    }

    public void uri(URI uri) {
        this.uri = uri;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public void putHeader(String name, String value) {
        if (name == null || name.isBlank() || value == null) {
            return;
        }
        headers.put(name, value);
    }

    public void removeHeader(String name) {
        if (name == null || name.isBlank()) {
            return;
        }
        headers.remove(name);
    }

    public Map<String, Object> queryParameters() {
        return queryParameters;
    }

    public void putQueryParameter(String name, Object value) {
        if (name == null || name.isBlank() || value == null) {
            return;
        }
        queryParameters.put(name, value);
    }

    public Map<String, Object> fields() {
        return fields;
    }

    public Object getField(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return fields.get(name);
    }

    public void putField(String name, Object value) {
        if (name == null || name.isBlank() || value == null) {
            return;
        }
        fields.put(name, value);
        if (body == null) {
            body = new LinkedHashMap<String, Object>();
        }
        if (!(body instanceof Map<?, ?>)) {
            throw new IllegalStateException("Provider request body does not support field enrichment");
        }
        Map<String, Object> bodyMap = mutableMap(body);
        bodyMap.put(name, value);
        body = bodyMap;
    }

    public Object body() {
        return body;
    }

    public void body(Object body) {
        this.body = mutableBody(body);
        fields.clear();
        copyBodyFields(this.body);
    }

    public Object nativeRequest() {
        return nativeRequest;
    }

    public void nativeRequest(Object nativeRequest) {
        this.nativeRequest = nativeRequest;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> bodyAsMap() {
        if (body == null) {
            body = new LinkedHashMap<String, Object>();
        }
        if (!(body instanceof Map<?, ?>)) {
            throw new IllegalStateException("Provider request body is not a map");
        }
        body = mutableMap(body);
        return (Map<String, Object>) body;
    }

    private Object mutableBody(Object source) {
        if (source instanceof Map<?, ?>) {
            return mutableMap(source);
        }
        return source;
    }

    private void copyBodyFields(Object source) {
        if (!(source instanceof Map<?, ?> map)) {
            return;
        }
        map.forEach((key, value) -> {
            if (key != null && value != null) {
                fields.put(String.valueOf(key), value);
            }
        });
    }

    private Map<String, Object> mutableMap(Object source) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (source instanceof Map<?, ?> map) {
            map.forEach((key, value) -> {
                if (key != null && value != null) {
                    result.put(String.valueOf(key), value);
                }
            });
        }
        return result;
    }
}
