package ir.daneshrefah.scm.core.integration.runtime;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScmRuntimeProperties {
    public static final String GATEWAY_NAME_PROPERTY = "scm.runtime.gateway-name";
    public static final String LEGACY_APP_NAME_PROPERTY = "scm.app-name";

    private final Environment environment;

    public String gatewayName() {
        String gatewayName = StringUtils.trimToNull(environment.getProperty(GATEWAY_NAME_PROPERTY));
        if (gatewayName != null) {
            return gatewayName;
        }
        String legacyAppName = StringUtils.trimToNull(environment.getProperty(LEGACY_APP_NAME_PROPERTY));
        if (legacyAppName != null) {
            return legacyAppName;
        }
        throw new IllegalStateException("Runtime gateway name is required. Configure "
                + GATEWAY_NAME_PROPERTY + " or legacy " + LEGACY_APP_NAME_PROPERTY + ".");
    }
}
