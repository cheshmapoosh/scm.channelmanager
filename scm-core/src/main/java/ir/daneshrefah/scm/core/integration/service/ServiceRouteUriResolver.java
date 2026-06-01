package ir.daneshrefah.scm.core.integration.service;

import ir.daneshrefah.scm.common.model.gateway.Service;

public interface ServiceRouteUriResolver {
    String resolve(Service service);

    String normalizeServiceCode(String serviceCode);
}
