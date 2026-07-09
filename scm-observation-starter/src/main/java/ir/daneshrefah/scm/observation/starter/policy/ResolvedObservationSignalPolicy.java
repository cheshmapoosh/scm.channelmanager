package ir.daneshrefah.scm.observation.starter.policy;

import ir.daneshrefah.scm.observation.starter.ObservationProperties;

public class ResolvedObservationSignalPolicy implements ObservationSignalPolicy {
    private final ObservationProperties properties;

    public ResolvedObservationSignalPolicy(ObservationProperties properties) {
        this.properties = properties == null ? new ObservationProperties() : properties;
    }

    @Override
    public boolean isEnabled(ObservationSignal signal) {
        if (signal == null || !properties.isEnabled()) {
            return false;
        }
        return switch (signal) {
            case LOG -> properties.getLog() != null && properties.getLog().isEnabled();
            case TRACE -> properties.getTrace() != null && properties.getTrace().isEnabled();
            case AUDIT -> properties.getAudit() != null && properties.getAudit().isEnabled();
            case METRIC -> properties.getMetric() != null && properties.getMetric().isEnabled();
        };
    }
}
