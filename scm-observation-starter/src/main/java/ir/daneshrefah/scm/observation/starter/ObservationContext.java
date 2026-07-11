package ir.daneshrefah.scm.observation.starter;

import org.springframework.core.env.Environment;

import java.time.ZoneId;

public record ObservationContext(
        boolean enabled,
        String namespace,
        String instanceId,
        ZoneId timeZone,
        String appName,
        String appProfile,
        String configLabel,
        String channelCode,
        String gatewayName,
        String serviceVersion,
        String runtime
) {
    public static ObservationContext from(ObservationProperties properties, Environment environment) {
        ObservationProperties safeProperties = properties == null ? new ObservationProperties() : properties;
        String appName = firstText(environmentValue(environment, "spring.application.name"), "application");
        String runtime = runtime(environment);
        return new ObservationContext(
                safeProperties.isEnabled(),
                firstText(environmentValue(environment, "scm.metadata.namespace"), "local"),
                firstText(environmentValue(environment, "scm.metadata.instance-id"), "local-" + appName),
                timeZone(environment),
                appName,
                firstText(firstActiveProfile(environment), environmentValue(environment, "spring.profiles.active"), "dev"),
                firstText(
                        environmentValue(environment, "spring.cloud.config.label"),
                        environmentValue(environment, "spring.cloud.config.server.git.default-label"),
                        "master"
                ),
                "default",
                "default",
                firstText(environmentValue(environment, "scm.deployment.service-version"), System.getenv("VERSION"), "unknown"),
                runtime
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

    private static ZoneId timeZone(Environment environment) {
        String configured = textOrNull(environmentValue(environment, "scm.metadata.time-zone"));
        if (configured == null) {
            return ZoneId.systemDefault();
        }
        try {
            return ZoneId.of(configured);
        } catch (RuntimeException ex) {
            throw new IllegalStateException("scm.metadata.time-zone must be a valid Java ZoneId.", ex);
        }
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
