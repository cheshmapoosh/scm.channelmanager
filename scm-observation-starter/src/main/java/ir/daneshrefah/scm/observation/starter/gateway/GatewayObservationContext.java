package ir.daneshrefah.scm.observation.starter.gateway;

public record GatewayObservationContext(
        String correlationId,
        String traceId,
        String gatewaySpanId,
        String remoteParentSpanId,
        String traceFlags,
        String gatewayName,
        String channelCode,
        String protocol,
        String spanKind,
        String requestName,
        String messageId
) {
    public static final String REQUEST_ATTRIBUTE = "scm.gateway.observation.context";
    public static final String CORRELATION_ID_ATTRIBUTE = "correlation.id";
    public static final String TRACE_ID_ATTRIBUTE = "scm.trace.id";
    public static final String GATEWAY_SPAN_ID_ATTRIBUTE = "scm.gateway.span.id";
    public static final String REMOTE_PARENT_SPAN_ID_ATTRIBUTE = "scm.remote.parent.span.id";
    public static final String TRACE_FLAGS_ATTRIBUTE = "scm.trace.flags";
    public static final String GATEWAY_NAME_ATTRIBUTE = "scm.gateway.name";
    public static final String CHANNEL_CODE_ATTRIBUTE = "scm.channel.code";
}
