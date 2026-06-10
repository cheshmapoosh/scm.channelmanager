package ir.daneshrefah.scm.core.integration.observability;

import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeTargetKind;
import org.apache.camel.Exchange;
import org.springframework.util.StringUtils;

import java.util.Locale;

public final class RouteLogSupport {
    public static final String GATEWAY_START_NANOS = "scmGatewayStartNanos";
    public static final String SERVICE_START_NANOS = "scmServiceStartNanos";
    public static final String OPERATION_START_NANOS = "scmOperationStartNanos";

    private static final long NANOS_PER_MILLISECOND = 1_000_000L;
    private static final int MAX_FAILURE_MESSAGE_LENGTH = 300;

    private RouteLogSupport() {
    }

    public static long durationMs(Exchange exchange, String startProperty) {
        Long startNanos = exchange.getProperty(startProperty, Long.class);
        if (startNanos == null) {
            return 0L;
        }
        return elapsedMs(startNanos);
    }

    public static long elapsedMs(long startNanos) {
        return Math.max(0L, (System.nanoTime() - startNanos) / NANOS_PER_MILLISECOND);
    }

    public static String failureType(Throwable exception) {
        return exception != null ? exception.getClass().getSimpleName() : null;
    }

    public static String failureMessage(Throwable exception) {
        if (exception == null || exception.getMessage() == null) {
            return null;
        }
        String message = exception.getMessage()
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("(?i)(password|token|authorization|client_secret|authorization_code|pin|otp|session[_-]?id|card[_-]?number)\\s*[:=]\\s*\\S+", "$1=***")
                .trim();
        if (message.length() > MAX_FAILURE_MESSAGE_LENGTH) {
            return message.substring(0, MAX_FAILURE_MESSAGE_LENGTH);
        }
        return message;
    }

    public static String gatewayName(Exchange exchange) {
        GatewayChannel gatewayChannel = exchange.getProperty(Message.GATEWAY_CHANNEL, GatewayChannel.class);
        if (gatewayChannel != null) {
            return gatewayChannel.getName();
        }
        return exchange.getProperty(Message.GATEWAY_NAME, String.class);
    }

    public static String targetKind(Exchange exchange) {
        RuntimeRoutePlan routePlan = exchange.getProperty(Message.RUNTIME_ROUTE_PLAN, RuntimeRoutePlan.class);
        RuntimeTargetKind targetKind = routePlan != null ? routePlan.targetKind() : null;
        return targetKind != null ? targetKind.name() : null;
    }

    public static String protocol(Exchange exchange) {
        Object protocol = exchange.getProperty(Message.GATEWAY_CHANNEL_PROTOCOL);
        return protocol != null ? String.valueOf(protocol) : null;
    }

    public static String channelCode(RuntimeServicePlan servicePlan) {
        return channelCode(servicePlan != null ? servicePlan.channelServiceAccess() : null);
    }

    public static String channelCode(ChannelServiceAccess access) {
        return access != null && access.getChannel() != null ? access.getChannel().getCode() : null;
    }

    public static Long channelServiceAccessId(RuntimeServicePlan servicePlan) {
        return servicePlan != null && servicePlan.channelServiceAccess() != null
                ? servicePlan.channelServiceAccess().getId()
                : null;
    }

    public static String serviceCode(RuntimeServicePlan servicePlan) {
        Service service = servicePlan != null ? servicePlan.service() : null;
        return service != null ? service.getCode() : null;
    }

    public static String normalizeForId(String value) {
        String normalized = StringUtils.trimWhitespace(value);
        if (!StringUtils.hasText(normalized)) {
            return null;
        }
        return normalized.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "")
                .replaceAll("-+", "-");
    }
}
