package ir.daneshrefah.scm.uaa.controller.token;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author noorafkan.m
 */
@RestController
@RequestMapping("/auth")
public class LegacyRefreshController {
    private final String legacyRefreshClientId;
    private final String legacyRefreshClientSecret;

    public LegacyRefreshController(
            @Value("${scm.uaa.legacy.refresh.client-id:MB}") String legacyRefreshClientId,
            @Value("${scm.uaa.legacy.refresh.client-secret:}") String legacyRefreshClientSecret
    ) {
        this.legacyRefreshClientId = legacyRefreshClientId;
        this.legacyRefreshClientSecret = legacyRefreshClientSecret;
    }

    @PostMapping(value = "/refresh", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void refresh(@RequestBody LegacyRefreshRequest body,
                        HttpServletRequest request,
                        HttpServletResponse response) throws Exception {

        Map<String, String[]> params = new HashMap<>();
        params.put("grant_type", new String[]{"ext_shk"});
        params.put("refresh_token", new String[]{body.refresh_token()});
        if (body.scope() != null && !body.scope().isBlank()) {
            params.put("scope", new String[]{body.scope()});
        }

        HttpServletRequest wrapped = new HeaderAndParamOverrideRequestWrapper(
                request,
                params,
                Map.of(HttpHeaders.AUTHORIZATION, basicAuthHeaderValue(legacyRefreshClientId, legacyRefreshClientSecret))
        );
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");


        RequestDispatcher dispatcher = wrapped.getRequestDispatcher("/oauth2/token");
        dispatcher.forward(wrapped, response);
    }

    public record LegacyRefreshRequest(String refresh_token, String scope, String clientId) {

    }

    private static String basicAuthHeaderValue(String clientId, String clientSecret) {
        if (clientId == null || clientSecret == null) {
            throw new IllegalStateException("Legacy refresh client-id/client-secret is not configured");
        }
        String raw = clientId + ":" + clientSecret;
        String b64 = Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        return "Basic " + b64;
    }

}


/**
 * Wrapper برای تزریق/override پارامترها
 */
final class HeaderAndParamOverrideRequestWrapper extends HttpServletRequestWrapper {

    private final Map<String, String[]> mergedParams;
    private final Map<String, String> injectedHeaders;

    public HeaderAndParamOverrideRequestWrapper(HttpServletRequest request,
                                                Map<String, String[]> extraParams,
                                                Map<String, String> injectedHeaders) {
        super(request);

        Map<String, String[]> tmp = new HashMap<>(request.getParameterMap());
        if (extraParams != null) tmp.putAll(extraParams); // override
        this.mergedParams = Collections.unmodifiableMap(tmp);

        Map<String, String> hdrs = new HashMap<>();
        if (injectedHeaders != null) {
            injectedHeaders.forEach((k, v) -> hdrs.put(k.toLowerCase(Locale.ROOT), v));
        }
        this.injectedHeaders = Collections.unmodifiableMap(hdrs);
    }

    // ---- params ----
    @Override public String getParameter(String name) {
        String[] values = mergedParams.get(name);
        return (values == null || values.length == 0) ? null : values[0];
    }

    @Override public Map<String, String[]> getParameterMap() {
        return mergedParams;
    }

    @Override public Enumeration<String> getParameterNames() {
        return Collections.enumeration(mergedParams.keySet());
    }

    @Override public String[] getParameterValues(String name) {
        return mergedParams.get(name);
    }

    // ---- headers ----
    @Override public String getHeader(String name) {
        if (name == null) return null;
        String injected = injectedHeaders.get(name.toLowerCase(Locale.ROOT));
        return (injected != null) ? injected : super.getHeader(name);
    }

    @Override public Enumeration<String> getHeaders(String name) {
        if (name == null) return super.getHeaders(null);

        String injected = injectedHeaders.get(name.toLowerCase(Locale.ROOT));
        if (injected == null) return super.getHeaders(name);

        // اگر قبلاً هم وجود داشت، injected را اول برگردان
        List<String> values = new ArrayList<>();
        values.add(injected);

        Enumeration<String> existing = super.getHeaders(name);
        while (existing.hasMoreElements()) values.add(existing.nextElement());

        return Collections.enumeration(values);
    }

    @Override public Enumeration<String> getHeaderNames() {
        Set<String> names = new LinkedHashSet<>();
        Enumeration<String> e = super.getHeaderNames();
        while (e.hasMoreElements()) names.add(e.nextElement());

        // نام‌هایی که تزریق شده‌اند را هم اضافه کن (با همان casing رایج)
        injectedHeaders.keySet().forEach(k -> {
            // normalize به Authorization و غیره
            if ("authorization".equals(k)) names.add(HttpHeaders.AUTHORIZATION);
            else names.add(k);
        });

        return Collections.enumeration(names);
    }
}
