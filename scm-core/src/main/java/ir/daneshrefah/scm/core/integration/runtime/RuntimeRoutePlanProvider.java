package ir.daneshrefah.scm.core.integration.runtime;

import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;

public interface RuntimeRoutePlanProvider {
    RuntimeRoutePlan provide(GatewayChannel gatewayChannel);
}
