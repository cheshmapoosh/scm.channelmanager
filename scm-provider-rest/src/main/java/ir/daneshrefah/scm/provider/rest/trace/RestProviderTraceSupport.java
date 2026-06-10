package ir.daneshrefah.scm.provider.rest.trace;

import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerContext;
import ir.daneshrefah.scm.observation.ObservationScope;
import ir.daneshrefah.scm.observation.ScmObservation;
import ir.daneshrefah.scm.provider.rest.config.RestProviderResolvedConfig;
import ir.daneshrefah.scm.provider.rest.customizer.RestAuthUrlProviderMessageCustomizerConfig;
import ir.daneshrefah.scm.provider.rest.model.RestProviderRequestSpec;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.function.Supplier;

@Component
public class RestProviderTraceSupport {
    private final ObjectProvider<ScmObservation> observationProvider;

    public RestProviderTraceSupport(ObjectProvider<ScmObservation> observationProvider) {
        this.observationProvider = observationProvider;
    }

    public ResponseEntity<String> clientSpan(
            Exchange exchange,
            RestProviderResolvedConfig config,
            RestProviderRequestSpec request,
            Supplier<ResponseEntity<String>> action
    ) {
        ScmObservation observation = observationProvider.getIfAvailable();
        if (observation == null) {
            return action.get();
        }

        URI uri = request == null ? null : request.uri();
        ObservationScope scope = observation.trace()
                .span("provider.rest.call")
                .spanKind("client")
                .action("provider.rest.call")
                .attribute("scm.provider.name", value(config == null ? null : config.provider()))
                .attribute("scm.provider.scheme", value(config == null ? null : config.scheme()))
                .attribute("scm.provider.uri", providerUri(config))
                .attribute("http.request.method", request == null || request.method() == null ? null : request.method().name())
                .attribute("net.peer.name", uri == null ? null : uri.getHost())
                .attribute("net.peer.port", uri == null || uri.getPort() <= 0 ? null : uri.getPort())
                .attribute("url.path", uri == null ? null : value(uri.getPath()))
                .start();

        try {
            ResponseEntity<String> response = action.get();
            if (response != null) {
                scope.attribute("http.response.status_code", response.getStatusCode().value());
            }
            scope.success();
            return response;
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

    public void tokenEvent(
            String eventName,
            RestProviderResolvedConfig providerConfig,
            RestAuthUrlProviderMessageCustomizerConfig authConfig,
            ProviderMessageCustomizerContext context
    ) {
        ScmObservation observation = observationProvider.getIfAvailable();
        if (observation == null) {
            return;
        }

        ObservationScope scope = observation.trace()
                .span("provider.auth.event")
                .spanKind("internal")
                .action(value(eventName))
                .attribute("scm.provider.name", value(providerConfig == null ? null : providerConfig.provider()))
                .attribute("scm.provider.scheme", value(providerConfig == null ? null : providerConfig.scheme()))
                .attribute("scm.provider.uri", providerUri(providerConfig))
                .attribute("scm.provider.service_code", serviceCode(context))
                .attribute("scm.provider.operation_code", operationCode(context))
                .attribute("scm.provider.channel_code", channelCode(context))
                .attribute("scm.provider.auth.profile", authConfig == null || authConfig.cache() == null ? null : authConfig.cache().getAuthProfile())
                .attribute("scm.provider.auth.cache_hit", cacheHit(eventName))
                .attribute("scm.provider.auth.lock_acquired", lockAcquired(eventName))
                .attribute("scm.provider.auth.token_refreshed", "provider.auth.token.refresh".equals(eventName) ? Boolean.TRUE : null)
                .start();
        try {
            scope.success();
        } catch (RuntimeException e) {
            scope.failure(e);
            throw e;
        } finally {
            scope.close();
        }
    }

    private String providerUri(RestProviderResolvedConfig config) {
        if (config == null) {
            return "";
        }
        return value(config.scheme()) + ":" + value(config.provider());
    }

    private Boolean cacheHit(String eventName) {
        if ("provider.auth.cache.hit".equals(eventName)) {
            return Boolean.TRUE;
        }
        if ("provider.auth.cache.miss".equals(eventName)) {
            return Boolean.FALSE;
        }
        return null;
    }

    private Boolean lockAcquired(String eventName) {
        if ("provider.auth.lock.acquired".equals(eventName)) {
            return Boolean.TRUE;
        }
        if ("provider.auth.lock.timeout".equals(eventName)) {
            return Boolean.FALSE;
        }
        return null;
    }

    private String channelCode(ProviderMessageCustomizerContext context) {
        return context == null ? "" : value(context.channelCode());
    }

    private String serviceCode(ProviderMessageCustomizerContext context) {
        return context == null ? "" : value(context.serviceCode());
    }

    private String operationCode(ProviderMessageCustomizerContext context) {
        return context == null ? "" : value(context.operationCode());
    }

    private String value(String value) {
        return value == null ? "" : value;
    }
}
