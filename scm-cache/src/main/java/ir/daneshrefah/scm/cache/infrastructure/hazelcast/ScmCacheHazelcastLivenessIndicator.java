package ir.daneshrefah.scm.cache.infrastructure.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("scmCacheHazelcastLiveness")
public class ScmCacheHazelcastLivenessIndicator implements HealthIndicator {
    private final ObjectProvider<HazelcastInstance> hazelcastInstanceProvider;

    public ScmCacheHazelcastLivenessIndicator(
            ObjectProvider<HazelcastInstance> hazelcastInstanceProvider
    ) {
        this.hazelcastInstanceProvider = hazelcastInstanceProvider;
    }

    @Override
    public Health health() {
        HazelcastInstance instance = hazelcastInstanceProvider.getIfAvailable();
        boolean running = instance != null
                && instance.getLifecycleService() != null
                && instance.getLifecycleService().isRunning();

        Health.Builder builder = running ? Health.up() : Health.down();
        return builder.withDetail("member.running", running).build();
    }
}
