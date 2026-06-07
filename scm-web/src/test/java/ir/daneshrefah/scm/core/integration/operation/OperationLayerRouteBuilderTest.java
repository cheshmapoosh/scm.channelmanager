package ir.daneshrefah.scm.core.integration.operation;

import ir.daneshrefah.scm.web.App;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@CamelSpringBootTest
@SpringBootTest(classes = App.class)
public class OperationLayerRouteBuilderTest {
    @Autowired
    private CamelContext camelContext;

    @Test
    void runtimeStartupCreatesOnlyCompactServiceAndOperationRoutes() {
        Set<String> routeIds = camelContext.getRoutes()
                .stream()
                .map(Route::getId)
                .collect(Collectors.toSet());
        Set<String> fromUris = camelContext.getRoutes()
                .stream()
                .map(route -> route.getEndpoint().getEndpointUri())
                .collect(Collectors.toSet());

        assertThat(routeIds).anyMatch(routeId -> routeId.startsWith("svc.dm."));
        assertThat(routeIds).anyMatch(routeId -> routeId.startsWith("op."));
        assertThat(routeIds).noneMatch(routeId -> routeId.startsWith("route-"));
        assertThat(routeIds).doesNotContain("op.SCMREAD_GETCUSTOMERACCOUNTS");
        assertThat(fromUris).doesNotContain("direct://SCMREAD_GETCUSTOMERACCOUNTS");
    }
}
