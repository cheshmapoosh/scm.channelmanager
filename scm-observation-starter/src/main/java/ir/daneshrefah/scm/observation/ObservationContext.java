package ir.daneshrefah.scm.observation;

import org.springframework.core.env.Environment;

import java.time.ZoneId;

public record ObservationContext(
        boolean enabled,
        String platform,
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
        return new ObservationContext(
                safeProperties.isEnabled(),
                "scm",
                firstText(environmentValue(environment, "spring.application.name"), "application"),
                firstText(firstActiveProfile(environment), "default"),
                "default",
                "default",
                "default",
                firstText(environmentValue(environment, "scm.deployment.service-version"), System.getenv("VERSION"), "unknown"),
                runtime(environment),
                OBSERVATION_ZONE_ID
        );
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

    private static String firstText(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate.trim();
            }
        }
        return "default";
    }
}
