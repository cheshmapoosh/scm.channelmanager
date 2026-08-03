package ir.daneshrefah.scm.web.health;

import ir.daneshrefah.scm.common.provider.readiness.ProviderReadinessContributor;
import ir.daneshrefah.scm.common.provider.readiness.ProviderReadinessContributor.ProviderReadiness;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component("providerReadiness")
public class ProviderReadinessHealthIndicator implements HealthIndicator {
    private final ObjectProvider<ProviderReadinessContributor> contributors;

    public ProviderReadinessHealthIndicator(ObjectProvider<ProviderReadinessContributor> contributors) {
        this.contributors = contributors;
    }

    @Override
    public Health health() {
        List<ProviderReadiness> snapshots = contributors.orderedStream()
                .flatMap(contributor -> contributor.readinessSnapshots().stream())
                .toList();
        if (snapshots.isEmpty()) {
            return Health.up().withDetail("effectiveProviderCount", 0).build();
        }

        boolean ready = snapshots.stream().allMatch(ProviderReadiness::ready);
        Map<String, Object> providers = new LinkedHashMap<>();
        for (ProviderReadiness snapshot : snapshots) {
            Map<String, Object> details = new LinkedHashMap<>(snapshot.details());
            details.put("status", snapshot.ready() ? "UP" : "DOWN");
            providers.put(snapshot.providerCode(), Map.copyOf(details));
        }

        Health.Builder builder = ready ? Health.up() : Health.down();
        return builder
                .withDetail("effectiveProviderCount", snapshots.size())
                .withDetail("providers", Map.copyOf(providers))
                .build();
    }
}
