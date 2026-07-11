package ir.daneshrefah.scm.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "scm.gateway.idempotency")
public class RestGatewayIdempotencyProperties {

    private boolean enabled = true;
    private List<String> apiPaths = new ArrayList<>();

    private String mapName = "rest-gateway-idempotent-repository";
}
