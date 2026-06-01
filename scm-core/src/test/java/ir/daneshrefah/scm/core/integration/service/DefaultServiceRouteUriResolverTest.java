package ir.daneshrefah.scm.core.integration.service;

import ir.daneshrefah.scm.common.model.gateway.Service;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultServiceRouteUriResolverTest {
    private final DefaultServiceRouteUriResolver resolver = new DefaultServiceRouteUriResolver();

    @Test
    void resolvesNormalizedServiceDirectUri() {
        Service service = new Service();
        service.setCode(" Card Inquiry ");

        assertEquals("direct:scm.service.card-inquiry", resolver.resolve(service));
    }
}
