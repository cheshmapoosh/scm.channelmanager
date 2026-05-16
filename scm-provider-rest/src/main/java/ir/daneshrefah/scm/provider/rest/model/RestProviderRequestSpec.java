package ir.daneshrefah.scm.provider.rest.model;

import org.springframework.http.HttpMethod;

import java.net.URI;
import java.util.Map;

public record RestProviderRequestSpec(
        HttpMethod method,
        URI uri,
        Map<String, String> headers,
        Object body
) {
}
