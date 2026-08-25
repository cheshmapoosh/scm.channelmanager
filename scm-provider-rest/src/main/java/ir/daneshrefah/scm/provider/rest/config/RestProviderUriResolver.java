package ir.daneshrefah.scm.provider.rest.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static ir.daneshrefah.scm.provider.rest.config.RestProviderTargetValidationException.OperationPathType;
import static ir.daneshrefah.scm.provider.rest.config.RestProviderTargetValidationException.Reason;

/**
 * Resolves the one immutable HTTP target owned by an effective REST Operation.
 */
@Component
public final class RestProviderUriResolver {

    public URI resolveOperationTarget(String baseUrl, String operationPath) {
        String configuredBaseUrl = StringUtils.trimToNull(baseUrl);
        ParsedOperationPath parsedPath = parseOperationPath(operationPath);

        if (configuredBaseUrl != null && parsedPath.type() == OperationPathType.ABSOLUTE) {
            throw invalid(
                    OperationPathType.ABSOLUTE,
                    Reason.BASE_URL_AND_ABSOLUTE_PATH_CONFLICT,
                    "REST Operation destination configuration is invalid: base-url and an absolute "
                            + "Operation.path cannot be configured together. Remove base-url or change "
                            + "Operation.path to a relative path."
            );
        }
        if (configuredBaseUrl == null && parsedPath.type() == OperationPathType.RELATIVE) {
            throw invalid(
                    OperationPathType.RELATIVE,
                    Reason.RELATIVE_PATH_WITHOUT_BASE_URL,
                    "REST Operation destination configuration is invalid: a relative Operation.path "
                            + "requires provider base-url. Configure base-url or use an absolute HTTP(S) "
                            + "Operation.path."
            );
        }
        if (parsedPath.type() == OperationPathType.INVALID) {
            throw invalid(
                    OperationPathType.INVALID,
                    Reason.INVALID_EFFECTIVE_TARGET,
                    "REST Operation destination configuration is invalid: Operation.path is not a valid "
                            + "absolute HTTP(S) URL or safe relative path."
            );
        }
        if (parsedPath.type() == OperationPathType.ABSOLUTE) {
            return requireAbsoluteOperationUri(parsedPath);
        }

        URI baseUri = requireBaseUri(configuredBaseUrl);
        String relativePath = requireRelativePath(parsedPath);
        try {
            URI target = UriComponentsBuilder.fromUri(baseUri)
                    .replacePath(joinPaths(baseUri.getPath(), relativePath))
                    .build()
                    .encode()
                    .toUri();
            validateHttpUri(target, OperationPathType.RELATIVE, Reason.INVALID_EFFECTIVE_TARGET);
            return target;
        } catch (RestProviderTargetValidationException exception) {
            throw exception;
        } catch (RuntimeException ignored) {
            throw invalid(
                    OperationPathType.RELATIVE,
                    Reason.INVALID_EFFECTIVE_TARGET,
                    "REST Operation destination configuration is invalid: base-url and Operation.path "
                            + "do not produce a valid HTTP(S) target."
            );
        }
    }

    public URI resolveDirectTarget(
            String baseUrl,
            String absoluteUrl,
            String path,
            Map<String, Object> query
    ) {
        String resolvedAbsolute = StringUtils.trimToNull(absoluteUrl);
        String resolvedBaseUrl = StringUtils.trimToNull(baseUrl);
        if (resolvedBaseUrl == null
                && (resolvedAbsolute == null || !isAbsoluteHttpUrl(resolvedAbsolute))) {
            throw missingDirectTarget();
        }
        try {
            UriComponentsBuilder builder;
            if (resolvedAbsolute != null) {
                builder = UriComponentsBuilder.fromUriString(resolvedAbsolute);
            } else {
                builder = UriComponentsBuilder.fromUriString(resolvedBaseUrl);
                String normalizedPath = StringUtils.trimToNull(path);
                if (normalizedPath != null) {
                    if (!normalizedPath.startsWith("/")) {
                        normalizedPath = "/" + normalizedPath;
                    }
                    builder.path(normalizedPath);
                }
            }
            appendQuery(builder, query);
            return builder.build().encode().toUri();
        } catch (RuntimeException ignored) {
            throw new IllegalArgumentException("REST provider direct call target URI is invalid");
        }
    }

    public URI appendQuery(URI uri, Map<String, ?> query) {
        Objects.requireNonNull(uri, "uri");
        if (query == null || query.isEmpty()) {
            return uri;
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        query.forEach((key, value) -> appendEncodedQueryValue(builder, key, value));
        return builder.build(true).toUri();
    }

    private ParsedOperationPath parseOperationPath(String operationPath) {
        String value = StringUtils.trimToNull(operationPath);
        if (value == null) {
            throw invalid(
                    OperationPathType.MISSING,
                    Reason.MISSING_OPERATION_PATH,
                    "REST Operation destination configuration is invalid: Operation.path is required. "
                            + "Configure a relative path with base-url or an absolute HTTP(S) URL without "
                            + "base-url."
            );
        }
        try {
            URI uri = URI.create(value);
            if (uri.isAbsolute()) {
                return new ParsedOperationPath(value, uri, OperationPathType.ABSOLUTE);
            }
            if (uri.getRawAuthority() != null || value.startsWith("//")) {
                return new ParsedOperationPath(value, uri, OperationPathType.INVALID);
            }
            return new ParsedOperationPath(value, uri, OperationPathType.RELATIVE);
        } catch (IllegalArgumentException ignored) {
            return new ParsedOperationPath(value, null, OperationPathType.INVALID);
        }
    }

    private URI requireBaseUri(String baseUrl) {
        URI uri;
        try {
            uri = URI.create(baseUrl);
        } catch (IllegalArgumentException ignored) {
            throw invalidBaseUrl();
        }
        validateHttpUri(uri, OperationPathType.RELATIVE, Reason.INVALID_BASE_URL);
        return uri;
    }

    private URI requireAbsoluteOperationUri(ParsedOperationPath parsedPath) {
        URI uri = parsedPath.uri();
        validateHttpUri(uri, OperationPathType.ABSOLUTE, Reason.INVALID_ABSOLUTE_OPERATION_URL);
        return uri;
    }

    private String requireRelativePath(ParsedOperationPath parsedPath) {
        URI uri = parsedPath.uri();
        String rawPath = uri.getRawPath();
        String decodedPath = uri.getPath();
        if (uri.getScheme() != null
                || uri.getRawAuthority() != null
                || uri.getRawQuery() != null
                || uri.getRawFragment() != null
                || StringUtils.isBlank(rawPath)
                || unsafePath(parsedPath.value(), rawPath, decodedPath)) {
            throw invalid(
                    OperationPathType.RELATIVE,
                    Reason.INVALID_RELATIVE_OPERATION_PATH,
                    "REST Operation destination configuration is invalid: relative Operation.path must "
                            + "be a safe path without authority, query, fragment, traversal, backslash, "
                            + "or duplicate separators."
            );
        }
        return decodedPath.startsWith("/") ? decodedPath : "/" + decodedPath;
    }

    private void validateHttpUri(URI uri, OperationPathType pathType, Reason reason) {
        String scheme = uri == null ? null : StringUtils.lowerCase(uri.getScheme(), Locale.ROOT);
        if (uri == null
                || !("http".equals(scheme) || "https".equals(scheme))
                || StringUtils.isBlank(uri.getHost())
                || uri.getRawUserInfo() != null
                || uri.getRawQuery() != null
                || uri.getRawFragment() != null
                || invalidPort(uri)
                || unsafePath(uri.toASCIIString(), uri.getRawPath(), uri.getPath())) {
            if (reason == Reason.INVALID_BASE_URL) {
                throw invalidBaseUrl();
            }
            String message = reason == Reason.INVALID_ABSOLUTE_OPERATION_URL
                    ? "REST Operation destination configuration is invalid: absolute Operation.path must "
                    + "be a safe HTTP(S) URL with a valid host and port and without user information, "
                    + "query, or fragment."
                    : "REST Operation destination configuration is invalid: the resolved HTTP(S) target "
                    + "is not valid or safe.";
            throw invalid(pathType, reason, message);
        }
    }

    private boolean invalidPort(URI uri) {
        int port = uri.getPort();
        if (port == 0 || port > 65_535) {
            return true;
        }
        String authority = uri.getRawAuthority();
        if (authority == null) {
            return false;
        }
        int portSeparator;
        if (authority.startsWith("[")) {
            int hostEnd = authority.indexOf(']');
            portSeparator = hostEnd >= 0
                    && hostEnd + 1 < authority.length()
                    && authority.charAt(hostEnd + 1) == ':'
                    ? hostEnd + 1
                    : -1;
        } else {
            portSeparator = authority.lastIndexOf(':');
        }
        return portSeparator >= 0 && port < 1;
    }

    private boolean unsafePath(String source, String rawPath, String decodedPath) {
        String normalizedRawPath = StringUtils.defaultString(rawPath).toLowerCase(Locale.ROOT);
        return decodedPath == null
                || StringUtils.defaultString(source).contains("\\")
                || normalizedRawPath.contains("//")
                || normalizedRawPath.contains("%2f")
                || normalizedRawPath.contains("%5c")
                || decodedPath.codePoints().anyMatch(Character::isISOControl)
                || containsDotSegment(decodedPath);
    }

    private boolean containsDotSegment(String path) {
        for (String segment : path.split("/", -1)) {
            if (".".equals(segment) || "..".equals(segment)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAbsoluteHttpUrl(String value) {
        try {
            URI uri = URI.create(value);
            String scheme = StringUtils.lowerCase(uri.getScheme(), Locale.ROOT);
            return uri.isAbsolute()
                    && ("http".equals(scheme) || "https".equals(scheme))
                    && StringUtils.isNotBlank(uri.getHost())
                    && uri.getRawUserInfo() == null
                    && uri.getRawFragment() == null
                    && !invalidPort(uri);
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private IllegalArgumentException missingDirectTarget() {
        return new IllegalArgumentException(
                "REST provider direct call requires base-url or an absolute HTTP(S) request URL"
        );
    }

    private String joinPaths(String basePath, String operationPath) {
        String normalizedBase = StringUtils.defaultString(basePath);
        while (normalizedBase.endsWith("/") && !normalizedBase.isEmpty()) {
            normalizedBase = normalizedBase.substring(0, normalizedBase.length() - 1);
        }
        return normalizedBase + operationPath;
    }

    private void appendQuery(UriComponentsBuilder builder, Map<String, ?> query) {
        Map<String, ?> queryMap = query == null ? Map.of() : query;
        queryMap.forEach((key, value) -> appendQueryValue(builder, key, value));
    }

    private void appendQueryValue(UriComponentsBuilder builder, String key, Object value) {
        if (StringUtils.isBlank(key) || value == null) {
            return;
        }
        if (value instanceof Collection<?> collection) {
            collection.forEach(item -> builder.queryParam(key, item));
            return;
        }
        builder.queryParam(key, value);
    }

    private void appendEncodedQueryValue(UriComponentsBuilder builder, String key, Object value) {
        if (StringUtils.isBlank(key) || value == null) {
            return;
        }
        String encodedKey = UriUtils.encodeQueryParam(key, StandardCharsets.UTF_8);
        if (value instanceof Collection<?> collection) {
            collection.stream()
                    .filter(Objects::nonNull)
                    .map(this::encodeQueryValue)
                    .forEach(item -> builder.queryParam(encodedKey, item));
            return;
        }
        builder.queryParam(encodedKey, encodeQueryValue(value));
    }

    private String encodeQueryValue(Object value) {
        return UriUtils.encodeQueryParam(String.valueOf(value), StandardCharsets.UTF_8);
    }

    private RestProviderTargetValidationException invalidBaseUrl() {
        return invalid(
                OperationPathType.RELATIVE,
                Reason.INVALID_BASE_URL,
                "REST Operation destination configuration is invalid: provider base-url must be an "
                        + "absolute HTTP(S) URL with a valid host and port and without user information, "
                        + "query, or fragment."
        );
    }

    private RestProviderTargetValidationException invalid(
            OperationPathType pathType,
            Reason reason,
            String message
    ) {
        return new RestProviderTargetValidationException(pathType, reason, message);
    }

    private record ParsedOperationPath(
            String value,
            URI uri,
            OperationPathType type
    ) {
    }
}
