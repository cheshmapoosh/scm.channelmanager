package ir.daneshrefah.scm.web.observation.gateway.http;

import ir.daneshrefah.scm.observation.starter.CorrelationType;
import ir.daneshrefah.scm.observation.starter.ObservationContext;
import ir.daneshrefah.scm.observation.starter.ObservationIds;
import ir.daneshrefah.scm.observation.starter.TraceFlags;
import ir.daneshrefah.scm.observation.starter.gateway.GatewayObservationContext;
import ir.daneshrefah.scm.observation.starter.attributes.trace.CommonTraceAttributes;
import ir.daneshrefah.scm.observation.starter.logging.ScmMdcKeys;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class HttpGatewayObservationFilter extends OncePerRequestFilter {
    private static final String DEFAULT_VALUE = "default";
    private static final String CORRELATION_HEADER = "X-Correlation-Id";
    private static final String LEGACY_CORRELATION_HEADER = "X-SCM-Correlation-ID";
    private static final List<String> OWNED_MDC_KEYS = List.of(
            ScmMdcKeys.CORRELATION_ID,
            ScmMdcKeys.CORRELATION_TYPE,
            ScmMdcKeys.TRACE_ID,
            ScmMdcKeys.SPAN_ID,
            ScmMdcKeys.GATEWAY_NAME,
            ScmMdcKeys.CHANNEL_CODE,
            ScmMdcKeys.PROTOCOL
    );
    private static final Set<String> MISSING_CHANNEL_CODES = Set.of(
            "null",
            "blank",
            "unknown",
            "default",
            "none",
            "n/a",
            "n-a"
    );

    private final ObservationContext observationContext;
    private final ScmTraceParentParser traceParentParser;
    private final boolean legacyGatewayEnabled;
    private final List<ChannelPathMapping> channelPathMappings;
    private final boolean trustedChannelHeaderEnabled;
    private final String trustedChannelHeaderName;
    private final List<String> allowedChannelCodes;
    private final List<LegacyPathMapping> legacyPathMappings;

    public HttpGatewayObservationFilter(
            ObservationContext observationContext,
            ScmTraceParentParser traceParentParser,
            @Value("${scm.web.observation.gateway.legacy.enabled:${SCM_WEB_OBS_LEGACY_GATEWAY_ENABLED:false}}") boolean legacyGatewayEnabled,
            @Value("${scm.web.observation.gateway.channel.path-prefix-mappings:${SCM_WEB_OBS_GATEWAY_CHANNEL_PATH_PREFIX_MAPPINGS:}}") String channelPathMappings,
            @Value("${scm.web.observation.gateway.channel.trusted-header.enabled:false}") boolean trustedChannelHeaderEnabled,
            @Value("${scm.web.observation.gateway.channel.trusted-header.name:X-SCM-Channel}") String trustedChannelHeaderName,
            @Value("${scm.runtime.channel-affinity.allowed-channel-codes:${SCM_CHANNEL_CODE:}}") String allowedChannelCodes,
            @Value("${scm.web.observation.gateway.legacy.route-mappings:${SCM_WEB_OBS_LEGACY_GATEWAY_ROUTE_MAPPINGS:}}") String legacyRouteMappings
    ) {
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
        String correlationId = effectiveCorrelationId(request);
        String channelCode = resolveChannelCode(request);
        GatewayObservationContext gatewayContext = new GatewayObservationContext(
                correlationId,
                incomingTraceParent == null ? ObservationIds.traceId() : incomingTraceParent.traceId(),
                ObservationIds.spanId(),
                incomingTraceParent == null ? null : incomingTraceParent.parentId(),
                incomingTraceParent == null ? TraceFlags.DEFAULT : incomingTraceParent.flags(),
                textOrDefault(observationContext.gatewayName()),
                channelCode,
                "http",
                "server",
                requestName(request),
                null
        );

        try {
            setRequestAttributes(request, gatewayContext);
            putSafeTransportAttributes(request);
            putLegacyProjectionAttributes(request);
            MdcSnapshot mdcSnapshot = MdcSnapshot.capture(OWNED_MDC_KEYS);
            putMdc(gatewayContext);
            response.setHeader(CORRELATION_HEADER, correlationId);
            try {
                filterChain.doFilter(request, response);
            } finally {
                mdcSnapshot.restore();
            }
        } finally {
            response.setHeader(CORRELATION_HEADER, correlationId);
        }
    }

    private String effectiveCorrelationId(HttpServletRequest request) {
        String inbound = firstText(
                request == null ? null : request.getHeader(CORRELATION_HEADER),
                request == null ? null : request.getHeader(LEGACY_CORRELATION_HEADER)
        );
        return inbound == null ? ObservationIds.correlationId() : inbound;
    }

    private void setRequestAttributes(HttpServletRequest request, GatewayObservationContext gatewayContext) {
        request.setAttribute(GatewayObservationContext.REQUEST_ATTRIBUTE, gatewayContext);
        request.setAttribute(GatewayObservationContext.CORRELATION_ID_ATTRIBUTE, gatewayContext.correlationId());
        request.setAttribute(GatewayObservationContext.TRACE_ID_ATTRIBUTE, gatewayContext.traceId());
        request.setAttribute(GatewayObservationContext.GATEWAY_SPAN_ID_ATTRIBUTE, gatewayContext.gatewaySpanId());
        request.setAttribute(GatewayObservationContext.REMOTE_PARENT_SPAN_ID_ATTRIBUTE, gatewayContext.remoteParentSpanId());
        request.setAttribute(GatewayObservationContext.TRACE_FLAGS_ATTRIBUTE, gatewayContext.traceFlags());
        request.setAttribute(GatewayObservationContext.GATEWAY_NAME_ATTRIBUTE, gatewayContext.gatewayName());
        request.setAttribute(GatewayObservationContext.CHANNEL_CODE_ATTRIBUTE, gatewayContext.channelCode());
    }

    private void putSafeTransportAttributes(HttpServletRequest request) {
        String clientIp = clientIp(request);
        request.setAttribute(CommonTraceAttributes.HTTP_METHOD.name(), textOrDefault(request.getMethod()));
        request.setAttribute(CommonTraceAttributes.URL_PATH.name(), safePath(request));
        request.setAttribute(CommonTraceAttributes.HTTP_QUERY_PRESENT.name(), hasQuery(request));
        request.setAttribute(CommonTraceAttributes.CLIENT_IP.name(), clientIp);
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

    private void putLegacyProjectionAttributes(HttpServletRequest request) {
        if (!legacyGatewayEnabled || request == null) {
            request.setAttribute("scm.observation.legacy.enabled", false);
            return;
        }
        LegacyPathMapping mapping = resolveLegacyMapping(safePath(request));
        if (mapping == null) {
            request.setAttribute("scm.observation.legacy.enabled", false);
            return;
        }
        request.setAttribute("scm.observation.legacy.enabled", true);
        request.setAttribute("scm.observation.legacy.service.code", mapping.serviceCode());
        request.setAttribute("scm.observation.legacy.operation.code", mapping.operationCode());
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
        MDC.put(ScmMdcKeys.CORRELATION_ID, gatewayContext.correlationId());
        MDC.put(ScmMdcKeys.CORRELATION_TYPE, CorrelationType.REQUEST.value());
        MDC.put(ScmMdcKeys.TRACE_ID, gatewayContext.traceId());
        MDC.put(ScmMdcKeys.SPAN_ID, gatewayContext.gatewaySpanId());
        MDC.put(ScmMdcKeys.GATEWAY_NAME, gatewayContext.gatewayName());
        MDC.put(ScmMdcKeys.CHANNEL_CODE, gatewayContext.channelCode());
        MDC.put(ScmMdcKeys.PROTOCOL, gatewayContext.protocol());
    }

    private String firstText(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String text = textOrNull(value);
            if (text != null) {
                return text;
            }
        }
        return null;
    }

    private record MdcSnapshot(Map<String, String> values) {
        private static MdcSnapshot capture(List<String> keys) {
            Map<String, String> values = new LinkedHashMap<>();
            for (String key : keys) {
                values.put(key, MDC.get(key));
            }
            return new MdcSnapshot(values);
        }

        private void restore() {
            values.forEach((key, previousValue) -> {
                if (previousValue == null) {
                    MDC.remove(key);
                } else {
                    MDC.put(key, previousValue);
                }
            });
        }
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
