package ir.daneshrefah.scm.common.provider.runtime;

/**
 * Optional lifecycle hook for provider transports that need an eagerly prepared runtime.
 * Core route construction reports only providers referenced by effective active services.
 */
public interface ProviderRuntimeLifecycle {

    boolean supports(String scheme);

    void registerEffectiveUsage(EffectiveProviderUsage usage);

    default void registrationComplete() {
    }

    record EffectiveProviderUsage(
            String serviceCode,
            String operationName,
            String operationPath,
            String providerCode,
            String providerUri,
            String scheme
    ) {
        public EffectiveProviderUsage(
                String serviceCode,
                String operationName,
                String providerCode,
                String providerUri,
                String scheme
        ) {
            this(serviceCode, operationName, null, providerCode, providerUri, scheme);
        }
    }
}
