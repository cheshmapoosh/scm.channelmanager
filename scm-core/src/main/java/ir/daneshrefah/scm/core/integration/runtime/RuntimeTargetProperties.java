package ir.daneshrefah.scm.core.integration.runtime;

import java.util.List;
import java.util.Objects;

public record RuntimeTargetProperties(
        RuntimeTargetKind targetKind,
        boolean enabled,
        List<String> gatewayNames) {

    public RuntimeTargetProperties {
        Objects.requireNonNull(targetKind, "targetKind must not be null");
        gatewayNames = gatewayNames == null ? List.of() : List.copyOf(gatewayNames);
    }
}
