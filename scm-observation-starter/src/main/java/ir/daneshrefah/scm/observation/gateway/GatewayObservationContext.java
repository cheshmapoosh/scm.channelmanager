package ir.daneshrefah.scm.observation.gateway;

public record GatewayObservationContext(
        String correlationId,
        String traceId,
        String gatewaySpanId,
        String gatewayName,
        String channelCode,
        GatewayProtocol protocol,
        String requestName,
        String messageId
) {
    public static final String REQUEST_ATTRIBUTE = "scm.gateway.observation.context";
    public static final String CORRELATION_ID_ATTRIBUTE = "correlation.id";
    public static final String TRACE_ID_ATTRIBUTE = "scm.trace.id";
    public static final String GATEWAY_SPAN_ID_ATTRIBUTE = "scm.gateway.span.id";
    public static final String GATEWAY_NAME_ATTRIBUTE = "scm.gateway.name";
    public static final String CHANNEL_CODE_ATTRIBUTE = "scm.channel.code";
}
