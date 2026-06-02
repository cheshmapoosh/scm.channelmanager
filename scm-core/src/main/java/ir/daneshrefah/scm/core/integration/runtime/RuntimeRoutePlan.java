package ir.daneshrefah.scm.core.integration.runtime;

import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;

import java.util.List;

public record RuntimeRoutePlan(
        GatewayChannel gatewayChannel,
        RuntimeTargetKind targetKind,
        List<RuntimeServicePlan> servicePlans) {
}
