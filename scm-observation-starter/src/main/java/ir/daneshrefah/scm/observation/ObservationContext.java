package ir.daneshrefah.scm.observation;

import org.springframework.core.env.Environment;

import java.time.ZoneId;

public record ObservationContext(
        boolean enabled,
        String platform,
        String namespace,
        String appName,
        String appProfile,
        String appLabel,
        String channelCode,
        String gatewayName,
        String serviceVersion,
        String runtime,
        ZoneId observationZoneId
) {
    private static final ZoneId OBSERVATION_ZONE_ID = ZoneId.of("UTC");

    public static ObservationContext from(ObservationProperties properties, Environment environment) {
        ObservationProperties safeProperties = properties == null ? new ObservationProperties() : properties;
        String runtime = runtime(environment);
        return new ObservationContext(
                safeProperties.isEnabled(),
                "scm",
                namespace(environment, runtime),
                firstText(environmentValue(environment, "spring.application.name"), "application"),
                firstText(environmentValue(environment, "deployment.environment"),
                        environmentValue(environment, "scm.env"),
                        firstActiveProfile(environment), "default"),
                "default",
                "default",
                "default",
                firstText(environmentValue(environment, "scm.deployment.service-version"), System.getenv("VERSION"), "unknown"),
                runtime,
                OBSERVATION_ZONE_ID
        );
    }

    private static String namespace(Environment environment, String runtime) {
        String namespace = textOrNull(environmentValue(environment, "SCM_OBS_NAMESPACE"));
        if (namespace == null) {
            namespace = textOrNull(System.getenv("SCM_OBS_NAMESPACE"));
        }
        if ("kubernetes".equals(runtime)) {
            return namespace;
        }
        return namespace == null ? "default" : namespace;
    }

    private static String runtime(Environment environment) {
        String configured = firstText(environmentValue(environment, "scm.runtime"), System.getenv("SCM_RUNTIME"), "");
        if ("kubernetes".equals(configured) || "standalone".equals(configured)) {
            return configured;
        }
        return environmentValue(environment, "KUBERNETES_SERVICE_HOST") == null
                && System.getenv("KUBERNETES_SERVICE_HOST") == null ? "standalone" : "kubernetes";
    }

    private static String firstActiveProfile(Environment environment) {
        if (environment == null) {
            return null;
        }
        String[] activeProfiles = environment.getActiveProfiles();
        return activeProfiles.length == 0 ? null : activeProfiles[0];
    }

    private static String environmentValue(Environment environment, String key) {
        return environment == null ? null : environment.getProperty(key);
    }

    private static String textOrNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String firstText(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate.trim();
            }
        }
        return "default";
    }
}
