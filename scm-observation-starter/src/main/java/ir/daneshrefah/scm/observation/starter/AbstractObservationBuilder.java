package ir.daneshrefah.scm.observation.starter;

import java.util.LinkedHashMap;
import java.util.Map;

abstract class AbstractObservationBuilder<T extends AbstractObservationBuilder<T>> {
    protected final ScmObservation observation;
    protected final Map<String, Object> attributes = new LinkedHashMap<>();
    protected String action;
    protected String outcome = "unknown";
    protected String correlationId;
    protected Class<?> sourceClass = ScmObservation.class;

    AbstractObservationBuilder(ScmObservation observation) {
        this.observation = observation;
    }

    public T action(String action) {
        this.action = action;
        return self();
    }

    public T operation(String operation) {
        return action(operation);
    }

    public T source(Class<?> sourceClass) {
        if (sourceClass != null) {
            this.sourceClass = sourceClass;
        }
        return self();
    }

    public T outcome(String outcome) {
        this.outcome = outcome;
        return self();
    }

    public T correlationId(String correlationId) {
        this.correlationId = correlationId;
        return self();
    }

    public T attribute(String name, Object value) {
        if (name != null && !name.isBlank() && value != null) {
            attributes.put(name, value);
        }
        return self();
    }

    public <V> T attribute(ObservationAttributeKey<V> key, V value) {
        if (key != null) {
            attribute(key.name(), value);
        }
        return self();
    }

    public T attributes(Map<String, ?> values) {
        if (values != null) {
            for (Map.Entry<String, ?> entry : values.entrySet()) {
                attribute(entry.getKey(), entry.getValue());
            }
        }
        return self();
    }

    protected abstract T self();
}
