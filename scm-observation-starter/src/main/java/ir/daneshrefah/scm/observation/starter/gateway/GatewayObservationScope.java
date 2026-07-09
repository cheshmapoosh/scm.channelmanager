package ir.daneshrefah.scm.observation.starter.gateway;

import ir.daneshrefah.scm.observation.starter.ObservationScope;

public final class GatewayObservationScope implements AutoCloseable {
    private final GatewayObservationLifecycle lifecycle;
    private final GatewayObservationRequest request;
    private final GatewayObservationContext context;
    private final ObservationScope traceScope;
    private final long startNanos;
    private GatewayObservationResult result;
    private boolean closed;

    GatewayObservationScope(
            GatewayObservationLifecycle lifecycle,
            GatewayObservationRequest request,
            GatewayObservationContext context,
            ObservationScope traceScope,
            long startNanos
    ) {
        this.lifecycle = lifecycle;
        this.request = request;
        this.context = context;
        this.traceScope = traceScope;
        this.startNanos = startNanos;
    }

    public GatewayObservationContext context() {
        return context;
    }

    public GatewayObservationScope success(GatewayObservationResult result) {
        if (!closed) {
            this.result = result == null ? GatewayObservationResult.success() : result;
        }
        return this;
    }

    public GatewayObservationScope failure(Throwable throwable) {
        return failure(GatewayObservationResult.failure(throwable));
    }

    public GatewayObservationScope failure(GatewayObservationResult result) {
        if (!closed) {
            this.result = result == null ? GatewayObservationResult.failure((Throwable) null) : result;
        }
        return this;
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            lifecycle.finish(request, context, traceScope, startNanos, result);
        }
    }
}
