package ir.daneshrefah.scm.common.provider.readiness;

import java.util.Collection;
import java.util.Map;

/**
 * Side-effect-free provider readiness snapshots consumed by an application-level health adapter.
 */
public interface ProviderReadinessContributor {

    Collection<ProviderReadiness> readinessSnapshots();

    record ProviderReadiness(
            String providerCode,
            boolean ready,
            Map<String, Object> details
    ) {
        public ProviderReadiness {
            details = details == null ? Map.of() : Map.copyOf(details);
        }
    }
}
