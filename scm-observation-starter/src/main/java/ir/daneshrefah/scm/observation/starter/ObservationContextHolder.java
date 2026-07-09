package ir.daneshrefah.scm.observation.starter;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class ObservationContextHolder {
    private static final AtomicReference<ObservationContext> CONTEXT = new AtomicReference<>();

    private ObservationContextHolder() {
    }

    public static void set(ObservationContext context) {
        if (context != null) {
            CONTEXT.set(context);
        }
    }

    public static Optional<ObservationContext> get() {
        return Optional.ofNullable(CONTEXT.get());
    }
}
