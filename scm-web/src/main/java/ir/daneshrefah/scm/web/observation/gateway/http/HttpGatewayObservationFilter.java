package ir.daneshrefah.scm.web.observation.gateway.http;

import ir.daneshrefah.scm.observation.ObservationContext;
import ir.daneshrefah.scm.observation.ObservationIds;
import ir.daneshrefah.scm.observation.TraceContext;
import ir.daneshrefah.scm.observation.TraceContextHolder;
import ir.daneshrefah.scm.observation.gateway.GatewayObservationContext;
import ir.daneshrefah.scm.observation.gateway.GatewayObservationLifecycle;
import ir.daneshrefah.scm.observation.gateway.GatewayObservationRequest;
import ir.daneshrefah.scm.observation.gateway.GatewayObservationResult;
import ir.daneshrefah.scm.observation.gateway.GatewayObservationScope;
import ir.daneshrefah.scm.observation.gateway.GatewayProtocol;
import ir.daneshrefah.scm.observation.attributes.trace.CommonTraceAttributes;
import ir.daneshrefah.scm.web.observation.propagation.ScmTraceParent;
import ir.daneshrefah.scm.web.observation.propagation.ScmTraceParentParser;
import ir.daneshrefah.scm.web.observation.propagation.ScmTraceParentWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class HttpGatewayObservationFilter extends OncePerRequestFilter {
    private static final String DEFAULT_VALUE = "default";
    private static final Set<String> MISSING_CHANNEL_CODES = Set.of(
            "null",
            "blank",
            "unknown",
            "default",
            "none",
            "n/a",
            "n-a"
    );

    private final GatewayObservationLifecycle gatewayObservationLifecycle;
    private final ObservationContext observationContext;
    private final ScmTraceParentParser traceParentParser;
    private final boolean legacyGatewayEnabled;
    private final List<ChannelPathMapping> channelPathMappings;
    private final boolean trustedChannelHeaderEnabled;
    private final String trustedChannelHeaderName;
    private final List<String> allowedChannelCodes;
    private final List<LegacyPathMapping> legacyPathMappings;

    public HttpGatewayObservationFilter(
            GatewayObservationLifecycle gatewayObservationLifecycle,
            ObservationContext observationContext,
            ScmTraceParentParser traceParentParser,
            @Value("${scm.web.observation.gateway.legacy.enabled:${SCM_WEB_OBS_LEGACY_GATEWAY_ENABLED:false}}") boolean legacyGatewayEnabled,
            @Value("${scm.web.observation.gateway.channel.path-prefix-mappings:${SCM_WEB_OBS_GATEWAY_CHANNEL_PATH_PREFIX_MAPPINGS:}}") String channelPathMappings,
            @Value("${scm.web.observation.gateway.channel.trusted-header.enabled:false}") boolean trustedChannelHeaderEnabled,
            @Value("${scm.web.observation.gateway.channel.trusted-header.name:X-SCM-Channel}") String trustedChannelHeaderName,
            @Value("${scm.runtime.channel-affinity.allowed-channel-codes:${SCM_CHANNEL_CODE:}}") String allowedChannelCodes,
            @Value("${scm.web.observation.gateway.legacy.route-mappings:${SCM_WEB_OBS_LEGACY_GATEWAY_ROUTE_MAPPINGS:}}") String legacyRouteMappings
    ) {
        this.gatewayObservationLifecycle = gatewayObservationLifecycle;
        this.observationContext = observationContext;
        this.traceParentParser = traceParentParser;
        this.legacyGatewayEnabled = legacyGatewayEnabled;
        this.channelPathMappings = parseChannelPathMappings(channelPathMappings);
        this.trustedChannelHeaderEnabled = trustedChannelHeaderEnabled;
        this.trustedChannelHeaderName = textOrDefault(trustedChannelHeaderName);
        this.allowedChannelCodes = parseAllowedChannels(allowedChannelCodes);
        this.legacyPathMappings = parseLegacyPathMappings(legacyRouteMappings);
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return true;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return true;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (request == null) {
            return true;
        }
        String path = safePath(request).toLowerCase(java.util.Locale.ROOT);
        return path.startsWith("/actuator")
                || path.startsWith("/internal")
                || path.startsWith("/admin")
                || path.startsWith("/config")
                || path.startsWith("/swagger")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/docs")
                || path.startsWith("/webjars")
                || path.startsWith("/assets")
                || path.startsWith("/static")
                || path.equals("/favicon.ico")
                || path.endsWith(".css")
                || path.endsWith(".js")
                || path.endsWith(".map")
                || path.endsWith(".png")
                || path.endsWith(".jpg")
                || path.endsWith(".jpeg")
                || path.endsWith(".svg")
                || path.endsWith(".ico");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        ScmTraceParent incomingTraceParent = traceParentParser.parse(request.getHeader(ScmTraceParentWriter.TRACEPARENT))
                .orElse(null);
        String correlationId = ObservationIds.correlationId();
        String channelCode = resolveChannelCode(request);
        GatewayObservationRequest.Builder requestBuilder = GatewayObservationRequest.builder()
                .protocol(GatewayProtocol.HTTP)
                .gatewayName(textOrDefault(observationContext.gatewayName()))
                .channelCode(channelCode)
                .correlationId(correlationId)
                .traceId(incomingTraceParent == null ? ObservationIds.traceId() : incomingTraceParent.traceId())
                .spanId(ObservationIds.spanId())
                .requestName(requestName(request))
                .clientAddress(clientIp(request))
                .attribute(CommonTraceAttributes.HTTP_METHOD, textOrDefault(request.getMethod()))
                .attribute(CommonTraceAttributes.URL_PATH, safePath(request))
                .attribute(CommonTraceAttributes.HTTP_QUERY_PRESENT, hasQuery(request))
                .attribute(CommonTraceAttributes.CLIENT_IP, clientIp(request));
        putLegacyProjectionAttributes(requestBuilder, request);
        GatewayObservationRequest observationRequest = requestBuilder.build();

        TraceContextHolder.Scope incomingParentScope = openIncomingParentScope(incomingTraceParent);
        GatewayObservationScope observationScope = null;

        try {
            observationScope = gatewayObservationLifecycle.start(observationRequest);
            GatewayObservationContext gatewayContext = observationScope.context();
            setRequestAttributes(request, gatewayContext);
            putMdc(gatewayContext);

            filterChain.doFilter(request, response);
            observationScope.success(GatewayObservationResult.builder()
                    .outcome("success")
                    .statusCode(response.getStatus())
                    .attribute(CommonTraceAttributes.HTTP_STATUS_CODE, response.getStatus())
                    .build());
        } catch (Throwable ex) {
            int statusCode = statusCode(response, ex);
            observationScope.failure(GatewayObservationResult.builder()
                    .outcome("failure")
                    .statusCode(statusCode)
                    .errorCode(errorCode(statusCode, ex))
                    .error(ex)
                    .attribute(CommonTraceAttributes.HTTP_STATUS_CODE, statusCode)
                    .build());
            rethrow(ex);
        } finally {
            if (observationScope != null) {
                observationScope.close();
            }
            clearMdc();
            closeIncomingParentScope(incomingParentScope);
        }
    }

    private void setRequestAttributes(HttpServletRequest request, GatewayObservationContext gatewayContext) {
        request.setAttribute(GatewayObservationContext.REQUEST_ATTRIBUTE, gatewayContext);
        request.setAttribute(GatewayObservationContext.CORRELATION_ID_ATTRIBUTE, gatewayContext.correlationId());
        request.setAttribute(GatewayObservationContext.TRACE_ID_ATTRIBUTE, gatewayContext.traceId());
        request.setAttribute(GatewayObservationContext.GATEWAY_SPAN_ID_ATTRIBUTE, gatewayContext.gatewaySpanId());
        request.setAttribute(GatewayObservationContext.GATEWAY_NAME_ATTRIBUTE, gatewayContext.gatewayName());
        request.setAttribute(GatewayObservationContext.CHANNEL_CODE_ATTRIBUTE, gatewayContext.channelCode());
    }

    private String requestName(HttpServletRequest request) {
        return "HTTP " + textOrDefault(request.getMethod());
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            int comma = forwardedFor.indexOf(',');
            String first = comma >= 0 ? forwardedFor.substring(0, comma) : forwardedFor;
            if (!first.isBlank()) {
                return first.trim();
            }
        }
        return textOrDefault(request.getRemoteAddr());
    }

    private String safePath(HttpServletRequest request) {
        return textOrDefault(request.getRequestURI());
    }

    private boolean hasQuery(HttpServletRequest request) {
        String query = request.getQueryString();
        return query != null && !query.isBlank();
    }

    private int statusCode(HttpServletResponse response, Throwable failure) {
        int statusCode = response.getStatus();
        if (failure != null && statusCode < 400) {
            return 500;
        }
        return statusCode;
    }

    private String errorCode(int statusCode, Throwable failure) {
        if (statusCode >= 400) {
            return "http.status." + statusCode;
        }
        return failure == null ? null : failure.getClass().getSimpleName();
    }

    private String textOrDefault(String value) {
        return value == null || value.isBlank() ? DEFAULT_VALUE : value.trim();
    }

    private String resolveChannelCode(HttpServletRequest request) {
        String path = safePath(request);
        String mappedChannel = resolveMappedChannel(path);
        if (mappedChannel != null) {
            return mappedChannel;
        }
        if (trustedChannelHeaderEnabled) {
            String headerChannel = normalizeChannelCode(request.getHeader(trustedChannelHeaderName));
            if (headerChannel != null && channelAllowed(headerChannel)) {
                return headerChannel;
            }
        }
        String affinityChannel = singleAllowedChannel();
        if (affinityChannel != null) {
            return affinityChannel;
        }
        String contextChannel = normalizeChannelCode(observationContext.channelCode());
        return contextChannel == null ? DEFAULT_VALUE : contextChannel;
    }

    private String resolveMappedChannel(String path) {
        for (ChannelPathMapping mapping : channelPathMappings) {
            if (mapping.matches(path)) {
                return mapping.channelCode();
            }
        }
        return null;
    }

    private void putLegacyProjectionAttributes(GatewayObservationRequest.Builder builder, HttpServletRequest request) {
        if (!legacyGatewayEnabled || request == null) {
            builder.attribute("scm.observation.legacy.enabled", false);
            return;
        }
        LegacyPathMapping mapping = resolveLegacyMapping(safePath(request));
        if (mapping == null) {
            builder.attribute("scm.observation.legacy.enabled", false);
            return;
        }
        builder.attribute("scm.observation.legacy.enabled", true)
                .attribute("scm.observation.legacy.service.code", mapping.serviceCode())
                .attribute("scm.observation.legacy.operation.code", mapping.operationCode());
    }

    private String textOrNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private LegacyPathMapping resolveLegacyMapping(String path) {
        for (LegacyPathMapping mapping : legacyPathMappings) {
            if (mapping.matches(path)) {
                return mapping;
            }
        }
        return null;
    }

    private List<ChannelPathMapping> parseChannelPathMappings(String value) {
        List<ChannelPathMapping> mappings = new ArrayList<>();
        for (String item : splitMappings(value)) {
            int separator = item.indexOf('=');
            if (separator <= 0 || separator >= item.length() - 1) {
                continue;
            }
            String prefix = normalizePathPrefix(item.substring(0, separator));
            String channel = normalizeChannelCode(item.substring(separator + 1));
            if (prefix != null && channel != null) {
                mappings.add(new ChannelPathMapping(prefix, channel));
            }
        }
        mappings.sort(Comparator.comparingInt(ChannelPathMapping::prefixLength).reversed());
        return List.copyOf(mappings);
    }

    private List<LegacyPathMapping> parseLegacyPathMappings(String value) {
        List<LegacyPathMapping> mappings = new ArrayList<>();
        for (String item : splitMappings(value)) {
            int separator = item.indexOf('=');
            if (separator <= 0 || separator >= item.length() - 1) {
                continue;
            }
            String prefix = normalizePathPrefix(item.substring(0, separator));
            String[] codes = splitLegacyCodes(item.substring(separator + 1));
            if (prefix != null && codes != null) {
                mappings.add(new LegacyPathMapping(prefix, codes[0], codes[1]));
            }
        }
        mappings.sort(Comparator.comparingInt(LegacyPathMapping::prefixLength).reversed());
        return List.copyOf(mappings);
    }

    private List<String> parseAllowedChannels(String value) {
        List<String> channels = new ArrayList<>();
        for (String item : splitMappings(value)) {
            String channel = normalizeChannelCode(item);
            if (channel != null) {
                channels.add(channel);
            }
        }
        return List.copyOf(channels);
    }

    private List<String> splitMappings(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(value.split("[,;]"))
                .map(String::trim)
                .filter(text -> !text.isBlank())
                .toList();
    }

    private String[] splitLegacyCodes(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String[] parts = value.trim().split("[:|/]", 2);
        if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            return null;
        }
        return new String[]{parts[0].trim(), parts[1].trim()};
    }

    private String normalizePathPrefix(String value) {
        String prefix = textOrNull(value);
        if (prefix == null) {
            return null;
        }
        return prefix.startsWith("/") ? prefix : "/" + prefix;
    }

    private String normalizeChannelCode(String value) {
        String channel = textOrNull(value);
        if (channel == null) {
            return null;
        }
        channel = channel.toLowerCase(Locale.ROOT);
        return MISSING_CHANNEL_CODES.contains(channel) ? null : channel;
    }

    private boolean channelAllowed(String channelCode) {
        return allowedChannelCodes.isEmpty()
                || allowedChannelCodes.contains("*")
                || allowedChannelCodes.contains(channelCode);
    }

    private String singleAllowedChannel() {
        if (allowedChannelCodes.size() == 1 && !"*".equals(allowedChannelCodes.getFirst())) {
            return allowedChannelCodes.getFirst();
        }
        return null;
    }

    private void putMdc(GatewayObservationContext gatewayContext) {
        MDC.put("correlationId", gatewayContext.correlationId());
        MDC.put("traceId", gatewayContext.traceId());
        MDC.put("spanId", gatewayContext.gatewaySpanId());
        MDC.put("gatewayName", gatewayContext.gatewayName());
        MDC.put("channelCode", gatewayContext.channelCode());
        MDC.put("protocol", gatewayContext.protocol().value());
    }

    private void clearMdc() {
        MDC.remove("correlationId");
        MDC.remove("traceId");
        MDC.remove("spanId");
        MDC.remove("gatewayName");
        MDC.remove("channelCode");
        MDC.remove("protocol");
    }

    private TraceContextHolder.Scope openIncomingParentScope(ScmTraceParent traceParent) {
        if (traceParent == null) {
            return null;
        }
        return TraceContextHolder.open(new TraceContext(traceParent.traceId(), traceParent.parentId(), null, null));
    }

    private void closeIncomingParentScope(TraceContextHolder.Scope scope) {
        if (scope == null) {
            return;
        }
        scope.close();
    }

    private void rethrow(Throwable throwable) throws ServletException, IOException {
        if (throwable instanceof ServletException servletException) {
            throw servletException;
        }
        if (throwable instanceof IOException ioException) {
            throw ioException;
        }
        if (throwable instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        if (throwable instanceof Error error) {
            throw error;
        }
        throw new ServletException(throwable);
    }

    private record ChannelPathMapping(String pathPrefix, String channelCode) {
        private boolean matches(String path) {
            return path != null && path.startsWith(pathPrefix);
        }

        private int prefixLength() {
            return pathPrefix.length();
        }
    }

    private record LegacyPathMapping(String pathPrefix, String serviceCode, String operationCode) {
        private boolean matches(String path) {
            return path != null && path.startsWith(pathPrefix);
        }

        private int prefixLength() {
            return pathPrefix.length();
        }
    }
}
