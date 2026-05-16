package ir.daneshrefah.scm.provider.rest.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.provider.rest.config.RestProviderResolvedConfig;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RestProviderLogSanitizerTest {

    private final RestProviderLogSanitizer sanitizer = new RestProviderLogSanitizer(new ObjectMapper());
    private final RestProviderResolvedConfig.Security security = new RestProviderResolvedConfig.Security(
            List.of("authorization", "x-api-key"),
            List.of("password", "token", "pin"),
            120
    );

    @Test
    void masksSensitiveHeadersAndBodyFields() {
        Map<String, Object> headers = Map.of(
                "Authorization", "Bearer abc",
                "X-Api-Key", "123",
                "X-Request-Id", "req-1"
        );
        Map<String, Object> body = Map.of(
                "username", "user",
                "password", "secret",
                "nested", Map.of("token", "jwt-value"),
                "pin", "1234"
        );

        Map<String, Object> safeHeaders = sanitizer.sanitizeHeaders(headers, security);
        Object safeBody = sanitizer.sanitizeBody(body, security);

        assertEquals("***", safeHeaders.get("Authorization"));
        assertEquals("***", safeHeaders.get("X-Api-Key"));
        assertEquals("req-1", safeHeaders.get("X-Request-Id"));

        Map<?, ?> safeBodyMap = (Map<?, ?>) safeBody;
        assertEquals("***", safeBodyMap.get("password"));
        assertEquals("***", safeBodyMap.get("pin"));
        Map<?, ?> nested = (Map<?, ?>) safeBodyMap.get("nested");
        assertEquals("***", nested.get("token"));
    }

    @Test
    void parsesJsonWhenPossible() {
        Object parsed = sanitizer.parseJsonIfPossible("{\"ok\":true}");
        assertTrue(parsed instanceof Map);
        Map<?, ?> json = (Map<?, ?>) parsed;
        assertEquals(true, json.get("ok"));
    }
}
