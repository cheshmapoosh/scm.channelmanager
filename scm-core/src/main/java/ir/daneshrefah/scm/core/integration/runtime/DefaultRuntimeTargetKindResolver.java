package ir.daneshrefah.scm.core.integration.runtime;

import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class DefaultRuntimeTargetKindResolver implements RuntimeTargetKindResolver {
    public static final String CHANNEL_PREFIX = "channel.";
    public static final String DOMAIN_PREFIX = "domain.";

    @Override
    public RuntimeTargetKind resolve(GatewayChannel gatewayChannel) {
        String name = gatewayChannel != null ? StringUtils.trimToNull(gatewayChannel.getName()) : null;
        if (name == null) {
            throw new IllegalArgumentException("GatewayChannel.name is required. Expected channel.<code> or domain.<code>.");
        }
        if (StringUtils.startsWith(name, CHANNEL_PREFIX) && name.length() > CHANNEL_PREFIX.length()) {
            return RuntimeTargetKind.CHANNEL;
        }
        if (StringUtils.startsWith(name, DOMAIN_PREFIX) && name.length() > DOMAIN_PREFIX.length()) {
            return RuntimeTargetKind.SERVICE_DOMAIN;
        }
        throw new IllegalArgumentException(
                "Invalid GatewayChannel.name '" + name + "'. Expected channel.<code> or domain.<code>.");
    }
}
