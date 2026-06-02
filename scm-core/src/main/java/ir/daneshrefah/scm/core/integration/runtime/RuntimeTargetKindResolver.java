package ir.daneshrefah.scm.core.integration.runtime;

import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;

public interface RuntimeTargetKindResolver {
    RuntimeTargetKind resolve(GatewayChannel gatewayChannel);
}
