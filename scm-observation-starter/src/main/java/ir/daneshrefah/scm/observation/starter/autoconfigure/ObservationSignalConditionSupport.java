package ir.daneshrefah.scm.observation.starter.autoconfigure;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

abstract class ObservationSignalConditionSupport implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        if (!enabled(context, "scm.observation.enabled")) {
            return false;
        }
        for (String property : signalProperties()) {
            if (enabled(context, property)) {
                return true;
            }
        }
        return false;
    }

    protected abstract String[] signalProperties();

    private boolean enabled(ConditionContext context, String property) {
        return context != null
                && context.getEnvironment() != null
                && Boolean.parseBoolean(context.getEnvironment().getProperty(property, "false"));
    }
}
