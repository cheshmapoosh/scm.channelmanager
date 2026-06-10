package ir.daneshrefah.scm.observation.gateway;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class GatewayObservationRequest {
    private final GatewayProtocol protocol;
    private final String gatewayName;
    private final String channelCode;
    private final String correlationId;
    private final String traceId;
    private final String spanId;
    private final String requestName;
    private final String messageId;
    private final String clientAddress;
    private final String routeId;
    private final Map<String, Object> attributes;

    private GatewayObservationRequest(Builder builder) {
        this.protocol = builder.protocol;
        this.gatewayName = builder.gatewayName;
        this.channelCode = builder.channelCode;
        this.correlationId = builder.correlationId;
        this.traceId = builder.traceId;
        this.spanId = builder.spanId;
        this.requestName = builder.requestName;
        this.messageId = builder.messageId;
        this.clientAddress = builder.clientAddress;
        this.routeId = builder.routeId;
        this.attributes = Collections.unmodifiableMap(new LinkedHashMap<>(builder.attributes));
    }

    public static Builder builder() {
        return new Builder();
    }

    public GatewayProtocol protocol() {
        return protocol;
    }

    public String gatewayName() {
        return gatewayName;
    }

    public String channelCode() {
        return channelCode;
    }

    public String correlationId() {
        return correlationId;
    }

    public String traceId() {
        return traceId;
    }

    public String spanId() {
        return spanId;
    }

    public String requestName() {
        return requestName;
    }

    public String messageId() {
        return messageId;
    }

    public String clientAddress() {
        return clientAddress;
    }

    public String routeId() {
        return routeId;
    }

    public Map<String, Object> attributes() {
        return attributes;
    }

    public static final class Builder {
        private GatewayProtocol protocol = GatewayProtocol.UNKNOWN;
        private String gatewayName;
        private String channelCode;
        private String correlationId;
        private String traceId;
        private String spanId;
        private String requestName;
        private String messageId;
        private String clientAddress;
        private String routeId;
        private final Map<String, Object> attributes = new LinkedHashMap<>();

        private Builder() {
        }

        public Builder protocol(GatewayProtocol protocol) {
            this.protocol = protocol == null ? GatewayProtocol.UNKNOWN : protocol;
            return this;
        }

        public Builder gatewayName(String gatewayName) {
            this.gatewayName = gatewayName;
            return this;
        }

        public Builder channelCode(String channelCode) {
            this.channelCode = channelCode;
            return this;
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder spanId(String spanId) {
            this.spanId = spanId;
            return this;
        }

        public Builder requestName(String requestName) {
            this.requestName = requestName;
            return this;
        }

        public Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder clientAddress(String clientAddress) {
            this.clientAddress = clientAddress;
            return this;
        }

        public Builder routeId(String routeId) {
            this.routeId = routeId;
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

        public GatewayObservationRequest build() {
            return new GatewayObservationRequest(this);
        }
    }
}
