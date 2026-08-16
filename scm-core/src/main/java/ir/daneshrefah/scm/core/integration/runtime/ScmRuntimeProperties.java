package ir.daneshrefah.scm.core.integration.runtime;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ScmRuntimeProperties {
    public static final String GATEWAY_NAME_PROPERTY = "scm.runtime.gateway-name";
    public static final String RUNTIME_TARGET_PROPERTY = "scm.runtime.target";
    public static final String RUNTIME_TARGETS_PROPERTY = "scm.runtime.targets";

    private final Environment environment;

    public String gatewayName() {
        String gatewayName = StringUtils.trimToNull(environment.getProperty(GATEWAY_NAME_PROPERTY));
        if (gatewayName != null) {
            return gatewayName;
        }
        throw new IllegalStateException("Runtime gateway name is required. Configure "
                + GATEWAY_NAME_PROPERTY + ".");
    }

    public List<RuntimeTargetProperties> runtimeTargets() {
        RuntimeTargetProperties configuredTarget = configuredRuntimeTarget();
        if (configuredTarget != null) {
            return List.of(configuredTarget);
        }

        RuntimeTargetConfiguration configuredTargets = configuredRuntimeTargets();
        if (configuredTargets.present()) {
            return configuredTargets.targets();
        }

        String gatewayName = gatewayName();
        return List.of(new RuntimeTargetProperties(
                targetKindFromGatewayName(gatewayName),
                true,
                List.of(gatewayName)));
    }

    public Map<RuntimeTargetKind, List<String>> gatewayNamesByTargetKind() {
        Map<RuntimeTargetKind, List<String>> gatewayNamesByKind = new EnumMap<>(RuntimeTargetKind.class);
        for (RuntimeTargetProperties runtimeTarget : runtimeTargets()) {
            if (!runtimeTarget.enabled()) {
                continue;
            }
            gatewayNamesByKind.computeIfAbsent(runtimeTarget.targetKind(), ignored -> new ArrayList<>())
                    .addAll(runtimeTarget.gatewayNames());
        }
        gatewayNamesByKind.replaceAll((ignored, gatewayNames) -> List.copyOf(gatewayNames));
        return Map.copyOf(gatewayNamesByKind);
    }

    private RuntimeTargetProperties configuredRuntimeTarget() {
        String prefix = RUNTIME_TARGET_PROPERTY;
        RuntimeTargetKind targetKind = runtimeTargetKind(prefix + ".kind");
        List<String> gatewayNames = gatewayNames(prefix + ".gateway-names");
        boolean configured = targetKind != null || !gatewayNames.isEmpty();
        if (!configured) {
            return null;
        }
        if (targetKind == null) {
            throw new IllegalStateException("Runtime target kind is required under " + prefix + ".kind.");
        }
        if (gatewayNames.isEmpty()) {
            throw new IllegalStateException("Runtime target " + targetKind
                    + " is configured but no gateway names are configured under "
                    + prefix + ".gateway-names.");
        }
        return new RuntimeTargetProperties(targetKind, true, gatewayNames);
    }

    private RuntimeTargetConfiguration configuredRuntimeTargets() {
        List<RuntimeTargetProperties> runtimeTargets = new ArrayList<>();
        boolean configured = false;
        configured |= addConfiguredTarget(runtimeTargets, RuntimeTargetKind.CHANNEL, "channel");
        configured |= addConfiguredTarget(runtimeTargets, RuntimeTargetKind.SERVICE_DOMAIN, "service-domain");
        return new RuntimeTargetConfiguration(configured, List.copyOf(runtimeTargets));
    }

    private boolean addConfiguredTarget(List<RuntimeTargetProperties> runtimeTargets,
                                        RuntimeTargetKind targetKind,
                                        String propertyName) {
        String prefix = RUNTIME_TARGETS_PROPERTY + "." + propertyName;
        List<String> gatewayNames = gatewayNames(prefix + ".gateway-names");
        Boolean enabled = environment.getProperty(prefix + ".enabled", Boolean.class);
        boolean configured = enabled != null || !gatewayNames.isEmpty();
        boolean targetEnabled = enabled != null ? enabled : !gatewayNames.isEmpty();

        if (!targetEnabled) {
            return configured;
        }
        if (gatewayNames.isEmpty()) {
            throw new IllegalStateException("Runtime target " + targetKind
                    + " is enabled but no gateway names are configured under "
                    + prefix + ".gateway-names.");
        }
        runtimeTargets.add(new RuntimeTargetProperties(targetKind, true, gatewayNames));
        return true;
    }

    private RuntimeTargetKind runtimeTargetKind(String propertyName) {
        String value = StringUtils.trimToNull(environment.getProperty(propertyName));
        if (value == null) {
            return null;
        }
        try {
            return RuntimeTargetKind.valueOf(value
                    .replace('-', '_')
                    .replace('.', '_')
                    .replace(' ', '_')
                    .toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid " + propertyName + " '" + value
                    + "'. Expected CHANNEL or SERVICE_DOMAIN.", exception);
        }
    }

    private List<String> gatewayNames(String propertyName) {
        LinkedHashSet<String> gatewayNames = new LinkedHashSet<>();
        String commaSeparated = environment.getProperty(propertyName);
        if (StringUtils.isNotBlank(commaSeparated)) {
            for (String gatewayName : commaSeparated.split(",")) {
                String normalized = StringUtils.trimToNull(gatewayName);
                if (normalized != null) {
                    gatewayNames.add(normalized);
                }
            }
        }

        for (int index = 0; ; index++) {
            String gatewayName = StringUtils.trimToNull(environment.getProperty(propertyName + "[" + index + "]"));
            if (gatewayName == null) {
                break;
            }
            gatewayNames.add(gatewayName);
        }
        return List.copyOf(gatewayNames);
    }

    private RuntimeTargetKind targetKindFromGatewayName(String gatewayName) {
        String normalizedGatewayName = StringUtils.trimToNull(gatewayName);
        if (normalizedGatewayName == null) {
            throw new IllegalStateException("Runtime gateway name is required.");
        }
        if (normalizedGatewayName.startsWith("channel.")) {
            return RuntimeTargetKind.CHANNEL;
        }
        if (normalizedGatewayName.startsWith("domain.")) {
            return RuntimeTargetKind.SERVICE_DOMAIN;
        }
        throw new IllegalArgumentException("Invalid runtime gateway name '" + normalizedGatewayName
                + "'. Expected channel.<code> or domain.<code>.");
    }

    private record RuntimeTargetConfiguration(boolean present, List<RuntimeTargetProperties> targets) {
    }
}
