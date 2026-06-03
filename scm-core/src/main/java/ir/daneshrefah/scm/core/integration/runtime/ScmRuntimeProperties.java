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
    public static final String RUNTIME_MODE_PROPERTY = "scm.runtime.mode";

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

    public RuntimeMode runtimeMode() {
        String runtimeMode = StringUtils.trimToNull(environment.getProperty(RUNTIME_MODE_PROPERTY));
        if (runtimeMode == null) {
            return RuntimeMode.DEFAULT;
        }
        try {
            return RuntimeMode.from(runtimeMode);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid " + RUNTIME_MODE_PROPERTY + " '" + runtimeMode
                    + "'. Expected CHANNEL, SERVICE_DOMAIN, or CHANNEL_AND_SERVICE_DOMAIN.", exception);
        }
    }
}
