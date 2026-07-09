package ir.daneshrefah.scm.observation.starter;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class ObservationAttributeRegistryHolder {
    private static final AtomicReference<ObservationAttributeRegistry> REGISTRY = new AtomicReference<>();

    private ObservationAttributeRegistryHolder() {
    }

    public static void set(ObservationAttributeRegistry registry) {
        if (registry != null) {
            REGISTRY.set(registry);
        }
    }

    public static Optional<ObservationAttributeRegistry> get() {
        return Optional.ofNullable(REGISTRY.get());
    }

    public static ObservationAttributeRegistry getOrCommonOnly() {
        ObservationAttributeRegistry registry = REGISTRY.get();
        return registry == null ? ObservationAttributeRegistry.commonOnly() : registry;
    }
}
