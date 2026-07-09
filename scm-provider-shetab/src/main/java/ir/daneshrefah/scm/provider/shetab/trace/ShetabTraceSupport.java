package ir.daneshrefah.scm.provider.shetab.trace;

import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerContext;
import ir.daneshrefah.scm.observation.starter.ObservationScope;
import ir.daneshrefah.scm.observation.starter.ScmObservation;
import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import org.apache.camel.Exchange;
import org.jpos.iso.ISOMsg;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Supplier;

@Component
public class ShetabTraceSupport {
    private final ObjectProvider<ScmObservation> observationProvider;

    public ShetabTraceSupport(ObjectProvider<ScmObservation> observationProvider) {
        this.observationProvider = observationProvider;
    }

    public <T> T clientSpan(Exchange exchange, ShetabResolvedConfig config, ISOMsg request, Supplier<T> action) {
        ScmObservation observation = observationProvider.getIfAvailable();
        if (observation == null) {
            return action.get();
        }

        String primaryEndpoint = primaryEndpoint(config);
        EndpointParts endpointParts = primaryEndpoint == null ? null : parseEndpoint(primaryEndpoint);

        ObservationScope scope = observation.trace()
                .span("provider.shetab.call")
                .spanKind("client")
                .action("provider.shetab.call")
                .attribute("scm.provider.name", value(config == null ? null : config.provider()))
                .attribute("scm.provider.scheme", value(config == null ? null : config.scheme()))
                .attribute("scm.provider.uri", providerUri(config))
                .attribute("shetab.endpoint.primary", primaryEndpoint)
                .attribute("net.peer.name", endpointParts == null ? null : endpointParts.host())
                .attribute("net.peer.port", endpointParts == null ? null : endpointParts.port())
                .attribute("shetab.iso.mti", safeMti(request))
                .attribute("shetab.iso.stan", safeField(request, 11))
                .attribute("shetab.iso.rrn", safeField(request, 37))
                .start();

        try {
            T result = action.get();
            scope.success();
            return result;
        } catch (RuntimeException e) {
            scope.failure(e);
            throw e;
        } finally {
            scope.close();
        }
    }

    public void customizerSpan(
            Exchange exchange,
            ProviderMessageCustomizerContext context,
            String customizerType,
            String phase,
            Runnable action
    ) {
        ScmObservation observation = observationProvider.getIfAvailable();
        if (observation == null) {
            action.run();
            return;
        }

        ObservationScope scope = observation.trace()
                .span("provider.customizer.execute")
                .spanKind("internal")
                .action("provider.customizer.execute")
                .attribute("scm.provider.name", value(context == null ? null : context.providerCode()))
                .attribute("scm.provider.scheme", value(context == null ? null : context.scheme()))
                .attribute("scm.provider.uri", value(context == null ? null : context.providerUri()))
                .attribute("scm.provider.service_code", value(context == null ? null : context.serviceCode()))
                .attribute("scm.provider.operation_code", value(context == null ? null : context.operationCode()))
                .attribute("scm.provider.channel_code", value(context == null ? null : context.channelCode()))
                .attribute("scm.provider.customizer.type", value(customizerType))
                .attribute("scm.provider.customizer.phase", value(phase))
                .start();

        try {
            action.run();
            scope.success();
        } catch (RuntimeException e) {
            scope.failure(e);
            throw e;
        } finally {
            scope.close();
        }
    }

    public void enrichLogMdc(Exchange exchange) {
        // Trace context is managed by scm-observation-starter/Micrometer.
        // Keep this method as a compatibility hook for existing producer code.
    }

    public Map<String, String> currentTraceIds() {
        return Map.of(
                "traceId", value(MDC.get("traceId")),
                "spanId", value(MDC.get("spanId"))
        );
    }

    private String providerUri(ShetabResolvedConfig config) {
        if (config == null) {
            return "";
        }
        return value(config.scheme()) + ":" + value(config.provider());
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    private String safeMti(ISOMsg msg) {
        try {
            return msg != null && msg.hasMTI() ? msg.getMTI() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private String safeField(ISOMsg msg, int field) {
        try {
            return msg != null ? msg.getString(field) : "";
        } catch (Exception e) {
            return "";
        }
    }

    private String primaryEndpoint(ShetabResolvedConfig config) {
        if (config == null || config.endpoints() == null || config.endpoints().isEmpty()) {
            return null;
        }
        String endpoint = config.endpoints().get(0);
        if (endpoint == null || endpoint.isBlank()) {
            return null;
        }
        return endpoint.trim();
    }

    private EndpointParts parseEndpoint(String endpoint) {
        int separator = endpoint.lastIndexOf(':');
        if (separator <= 0 || separator == endpoint.length() - 1) {
            return null;
        }
        try {
            String host = endpoint.substring(0, separator).trim();
            int port = Integer.parseInt(endpoint.substring(separator + 1).trim());
            if (host.isBlank() || port < 1) {
                return null;
            }
            return new EndpointParts(host, port);
        } catch (Exception ignored) {
            return null;
        }
    }

    private record EndpointParts(String host, int port) {
    }
}
