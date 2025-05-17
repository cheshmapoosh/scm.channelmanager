package ir.daneshrefah.scm.core.services.gateway;

import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;

public interface GatewayService {
    GatewayChannel findGatewayChannelByName(String name);
}
