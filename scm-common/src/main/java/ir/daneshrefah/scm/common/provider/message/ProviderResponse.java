package ir.daneshrefah.scm.common.provider.message;

import java.util.LinkedHashMap;
import java.util.Map;

public class ProviderResponse {
    private Integer statusCode;
    private final Map<String, Object> headers = new LinkedHashMap<>();
    private Object body;
    private Object nativeResponse;

    public ProviderResponse() {
    }

    public ProviderResponse(Integer statusCode, Map<String, ?> headers, Object body) {
        this.statusCode = statusCode;
        if (headers != null) {
            headers.forEach((key, value) -> {
                if (key != null && value != null) {
                    this.headers.put(key, value);
                }
            });
        }
        this.body = body;
    }

    public Integer statusCode() {
        return statusCode;
    }

    public void statusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Map<String, Object> headers() {
        return headers;
    }

    public Object body() {
        return body;
    }

    public void body(Object body) {
        this.body = body;
    }

    public Object nativeResponse() {
        return nativeResponse;
    }

    public void nativeResponse(Object nativeResponse) {
        this.nativeResponse = nativeResponse;
    }
}
