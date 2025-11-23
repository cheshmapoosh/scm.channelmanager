package ir.daneshrefah.scm.common.service;

import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;

import java.util.List;

public interface GatewayService {
    GatewayChannel findGatewayChannelByName(String name);
    GatewayChannel findById(String id);
    List<GatewayChannel> findAll();
}
