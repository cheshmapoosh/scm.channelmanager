package ir.daneshrefah.scm.provider.rest.config;

import ir.daneshrefah.scm.common.provider.runtime.ProviderRuntimeLifecycle;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Startup-built catalog of effective REST Operation targets.
 */
@Component
@Slf4j
public final class RestProviderOperationTargetRegistry implements ProviderRuntimeLifecycle {

    private final RestProviderConfigResolver configResolver;
    private final RestProviderUriResolver uriResolver;
    private final Map<String, OperationTarget> registrations = new LinkedHashMap<>();
    private volatile Map<String, OperationTarget> targets = Map.of();
    private boolean registrationComplete;

    public RestProviderOperationTargetRegistry(
            RestProviderConfigResolver configResolver,
            RestProviderUriResolver uriResolver
    ) {
        this.configResolver = configResolver;
        this.uriResolver = uriResolver;
    }

    @Override
    public boolean supports(String scheme) {
        return RestProviderConfigResolver.COMPONENT_SCHEME.equalsIgnoreCase(scheme);
    }

    @Override
    public synchronized void registerEffectiveUsage(EffectiveProviderUsage usage) {
        Objects.requireNonNull(usage, "usage");
        if (!supports(usage.scheme())) {
            return;
        }
        if (registrationComplete) {
            throw new IllegalStateException("REST provider runtime registration is already complete");
        }

        String operationName = requireOperationName(usage.operationName());
        RestProviderResolvedConfig config = configResolver.resolve(usage.providerUri(), null);
        try {
            URI targetUri = uriResolver.resolveOperationTarget(config.baseUrl(), usage.operationPath());
            OperationTarget target = new OperationTarget(
                    operationName,
                    config.provider(),
                    config.scheme() + ":" + config.provider(),
                    targetUri
            );
            OperationTarget existing = registrations.putIfAbsent(operationName, target);
            if (existing != null && !existing.equals(target)) {
                throw new IllegalStateException("Conflicting REST provider target for operation '"
                        + operationName + "'");
            }
        } catch (RestProviderTargetValidationException failure) {
            logTargetValidationFailure(usage, config, failure);
            throw failure;
        }
    }

    @Override
    public synchronized void registrationComplete() {
        if (registrationComplete) {
            return;
        }
        targets = Collections.unmodifiableMap(new LinkedHashMap<>(registrations));
        registrationComplete = true;
    }

    public OperationTarget requireTarget(String operationName) {
        String name = requireOperationName(operationName);
        OperationTarget target = targets.get(name);
        if (target == null) {
            throw new IllegalStateException("REST provider target is not registered for operation '"
                    + name + "'");
        }
        return target;
    }

    private void logTargetValidationFailure(
            EffectiveProviderUsage usage,
            RestProviderResolvedConfig config,
            RestProviderTargetValidationException failure
    ) {
        log.error(
                "event=rest_provider_operation_target_validation_failed layer=provider providerType=rest "
                        + "serviceCode={} operationName={} providerCode={} baseUrlConfigured={} "
                        + "operationPathType={} reason={} outcome=failed message={}",
                safeContext(usage.serviceCode()),
                safeContext(usage.operationName()),
                safeContext(config.provider()),
                StringUtils.isNotBlank(config.baseUrl()),
                failure.operationPathType().name(),
                failure.reason().name(),
                failure.getMessage()
        );
    }

    private String safeContext(String value) {
        String text = StringUtils.trimToNull(value);
        if (text == null) {
            return "<unknown>";
        }
        return text.replace('\r', '_').replace('\n', '_');
    }

    private String requireOperationName(String operationName) {
        String value = StringUtils.trimToNull(operationName);
        if (value == null) {
            throw new IllegalStateException("REST provider Operation name is required");
        }
        return value;
    }

    public record OperationTarget(
            String operationName,
            String providerCode,
            String providerUri,
            URI uri
    ) {
    }
}
