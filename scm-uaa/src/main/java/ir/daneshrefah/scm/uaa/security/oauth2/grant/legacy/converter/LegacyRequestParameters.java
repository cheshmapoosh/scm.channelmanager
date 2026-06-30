package ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.converter;

import ir.daneshrefah.scm.uaa.common.utils.Constants;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Normalized legacy request parameters kept only for old NIB/PWA/MB/SA compatibility.
 * Remove this class after migration to UAA-hosted login and authorization-code flow is complete.
 * No new feature should be added here unless strictly required for migration safety.
 */
@Deprecated(since = "9.0.0", forRemoval = true)
@SuppressWarnings("removal")
public final class LegacyRequestParameters {
    private static final String[] APP_VERSION_NAMES = {
            "AppVersion",
            "Appversion",
            "app_version",
            "app-version",
            "APP_VERSION",
            "APPVERSION",
            Constants.APP_VERSION_HEADER
    };

    private final Map<String, List<String>> parameters;
    private final Map<String, List<String>> headers;

    public LegacyRequestParameters(HttpServletRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        Map<String, List<String>> normalizedParameters = new LinkedHashMap<>();
        Map<String, List<String>> normalizedHeaders = new LinkedHashMap<>();
        addParameters(request, normalizedParameters);
        addHeaders(request, normalizedHeaders);
        parameters = immutableCopy(normalizedParameters);
        headers = immutableCopy(normalizedHeaders);
    }

    public Optional<String> first(String name) {
        return first(parameters, name).or(() -> first(headers, name));
    }

    public Optional<String> firstParameter(String name) {
        return first(parameters, name);
    }

    private Optional<String> first(Map<String, List<String>> source, String name) {
        if (name == null) {
            return Optional.empty();
        }
        return source.getOrDefault(normalize(name), List.of())
                .stream()
                .filter(Objects::nonNull)
                .findFirst();
    }

    public Optional<String> firstAny(String... names) {
        if (names == null) {
            return Optional.empty();
        }
        for (String name : names) {
            Optional<String> value = first(name);
            if (value.isPresent()) {
                return value;
            }
        }
        return Optional.empty();
    }

    public Optional<String> appVersion() {
        return firstAny(headers, APP_VERSION_NAMES)
                .or(() -> firstAny(parameters, APP_VERSION_NAMES));
    }

    private Optional<String> firstAny(Map<String, List<String>> source, String... names) {
        for (String name : names) {
            Optional<String> value = first(source, name);
            if (value.isPresent()) {
                return value;
            }
        }
        return Optional.empty();
    }

    private void addHeaders(HttpServletRequest request, Map<String, List<String>> normalizedValues) {
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames == null) {
            return;
        }
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            Enumeration<String> headerValues = request.getHeaders(name);
            if (headerValues == null) {
                continue;
            }
            while (headerValues.hasMoreElements()) {
                add(normalizedValues, name, headerValues.nextElement());
            }
        }
    }

    private void addParameters(HttpServletRequest request, Map<String, List<String>> normalizedValues) {
        request.getParameterMap().forEach((name, parameterValues) -> {
            if (parameterValues == null) {
                return;
            }
            for (String value : parameterValues) {
                add(normalizedValues, name, value);
            }
        });
    }

    private void add(Map<String, List<String>> normalizedValues, String name, String value) {
        if (name == null) {
            return;
        }
        normalizedValues.computeIfAbsent(normalize(name), ignored -> new ArrayList<>()).add(value);
    }

    private String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }

    private Map<String, List<String>> immutableCopy(Map<String, List<String>> source) {
        Map<String, List<String>> copy = new LinkedHashMap<>(source.size());
        source.forEach((key, entryValues) ->
                copy.put(key, Collections.unmodifiableList(new ArrayList<>(entryValues))));
        return Collections.unmodifiableMap(copy);
    }
}
