package ir.daneshrefah.scm.observation.gateway;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class GatewayObservationResult {
    private final String outcome;
    private final Integer statusCode;
    private final String errorCode;
    private final String errorType;
    private final String errorMessage;
    private final Map<String, Object> attributes;

    private GatewayObservationResult(Builder builder) {
        this.outcome = builder.outcome;
        this.statusCode = builder.statusCode;
        this.errorCode = builder.errorCode;
        this.errorType = builder.errorType;
        this.errorMessage = builder.errorMessage;
        this.attributes = Collections.unmodifiableMap(new LinkedHashMap<>(builder.attributes));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static GatewayObservationResult success() {
        return builder().outcome("success").build();
    }

    public static GatewayObservationResult success(Integer statusCode) {
        return builder().outcome("success").statusCode(statusCode).build();
    }

    public static GatewayObservationResult failure(Throwable throwable) {
        return builder().outcome("failure").error(throwable).build();
    }

    public static GatewayObservationResult failure(String errorCode, Throwable throwable) {
        return builder().outcome("failure").errorCode(errorCode).error(throwable).build();
    }

    public String outcome() {
        return outcome;
    }

    public Integer statusCode() {
        return statusCode;
    }

    public String errorCode() {
        return errorCode;
    }

    public String errorType() {
        return errorType;
    }

    public String errorMessage() {
        return errorMessage;
    }

    public Map<String, Object> attributes() {
        return attributes;
    }

    public static final class Builder {
        private String outcome = "unknown";
        private Integer statusCode;
        private String errorCode;
        private String errorType;
        private String errorMessage;
        private final Map<String, Object> attributes = new LinkedHashMap<>();

        private Builder() {
        }

        public Builder outcome(String outcome) {
            this.outcome = outcome;
            return this;
        }

        public Builder statusCode(Integer statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder errorType(String errorType) {
            this.errorType = errorType;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder error(Throwable throwable) {
            if (throwable != null) {
                this.errorType = throwable.getClass().getName();
                this.errorMessage = throwable.getMessage();
            }
            return this;
        }

        public Builder attribute(String name, Object value) {
            if (name != null && !name.isBlank() && value != null) {
                this.attributes.put(name.trim(), value);
            }
            return this;
        }

        public <V> Builder attribute(ObservationAttributeKey<V> key, V value) {
            if (key != null) {
                attribute(key.name(), value);
            }
            return this;
        }

        public Builder attributes(Map<String, ?> attributes) {
            if (attributes != null) {
                attributes.forEach(this::attribute);
            }
            return this;
        }

        public GatewayObservationResult build() {
            return new GatewayObservationResult(this);
        }
    }
}
