package ir.daneshrefah.scm.core.integration.service.lookup;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;

public record EbServiceSnapshot(
        Long id,
        String code,
        RoutingStrategy routingStrategy,
        boolean active
) {
}
