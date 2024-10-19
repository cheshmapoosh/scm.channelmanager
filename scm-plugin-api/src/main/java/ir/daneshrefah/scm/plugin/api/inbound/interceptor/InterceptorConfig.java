package ir.daneshrefah.scm.plugin.api.inbound.interceptor;

import lombok.Getter;

@Getter
public class InterceptorConfig {
    private int order = 999;
    private Type type;
    private InterceptorConfig() {
    }

    public static InterceptorConfig create() {
        return new InterceptorConfig();
    }

    public InterceptorConfig order(int order) {
        this.order = order;
        return this;
    }

    public InterceptorConfig type(Type type) {
        this.type = type;
        return this;
    }

    public InterceptorConfig build() {
        return this;
    }

    public enum Type {
        REQUEST, RESPONSE
    }
}
