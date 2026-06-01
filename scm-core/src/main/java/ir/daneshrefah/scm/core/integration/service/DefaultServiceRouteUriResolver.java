package ir.daneshrefah.scm.core.integration.service;

import ir.daneshrefah.scm.common.model.gateway.Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class DefaultServiceRouteUriResolver implements ServiceRouteUriResolver {
    private static final String SERVICE_ROUTE_PREFIX = "direct:scm.service.";

    @Override
    public String resolve(Service service) {
        if (service == null) {
            throw new IllegalArgumentException("Service is required for service route URI resolution.");
        }
        return SERVICE_ROUTE_PREFIX + normalizeServiceCode(service.getCode());
    }

    @Override
    public String normalizeServiceCode(String serviceCode) {
        String normalized = StringUtils.trimToNull(serviceCode);
        if (normalized == null) {
            throw new IllegalArgumentException("Service code is required for service route URI resolution.");
        }
        normalized = normalized.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
        if (StringUtils.isBlank(normalized)) {
            throw new IllegalArgumentException("Service code '" + serviceCode + "' cannot be normalized.");
        }
        return normalized;
    }
}
