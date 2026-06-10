package ir.daneshrefah.scm.cache.observation;

import ir.daneshrefah.scm.observation.logging.ScmLogFields;
import ir.daneshrefah.scm.observation.logging.ScmLogMarkers;
import ir.daneshrefah.scm.observation.logging.ScmInitCorrelationContext;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
public class ScmCacheInitLogging implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ensureInitCorrelationId();
        log.info(
                ScmLogMarkers.SCM_INIT,
                "SCM runtime context created",
                contextArguments(applicationContext.getEnvironment())
        );
        log.info(
                ScmLogMarkers.SCM_INIT,
                "SCM cache init started",
                initArguments("scm.cache.init.started", "unknown")
        );
    }

    public static void ensureInitCorrelationId() {
        ScmInitCorrelationContext.ensure();
    }

    public static Object[] initArguments(String action, String outcome, Object... additionalFields) {
        ensureInitCorrelationId();
        List<Object> arguments = new ArrayList<>();
        arguments.add(kv(ScmLogFields.EVENT_CATEGORY, "scm.init"));
        arguments.add(kv(ScmLogFields.EVENT_ACTION, action));
        arguments.add(kv(ScmLogFields.EVENT_OUTCOME, outcome));
        for (Object additionalField : additionalFields) {
            if (additionalField != null) {
                arguments.add(additionalField);
            }
        }
        return arguments.toArray();
    }

    public static Object kv(String name, Object value) {
        return StructuredArguments.kv(name, value);
    }

    private static Object[] contextArguments(Environment environment) {
        ensureInitCorrelationId();
        List<Object> arguments = new ArrayList<>();
        arguments.add(kv(ScmLogFields.EVENT_CATEGORY, "scm.context"));
        arguments.add(kv(ScmLogFields.EVENT_ACTION, "runtime.context.created"));
        arguments.add(kv(ScmLogFields.EVENT_OUTCOME, "success"));
        arguments.add(kv(ScmLogFields.DEPLOYMENT_SERVICE_NAME, firstPresent(
                environment.getProperty("spring.application.name"),
                "scm-cache"
        )));
        arguments.add(kv(ScmLogFields.DEPLOYMENT_SERVICE_VERSION, firstPresent(
                environment.getProperty("scm.deployment.service-version"),
                System.getenv("VERSION"),
                implementationVersion()
        )));
        arguments.add(kv(ScmLogFields.DEPLOYMENT_ENVIRONMENT, deploymentEnvironment(environment)));
        arguments.add(kv(ScmLogFields.SCM_RUNTIME, runtime(environment)));
        arguments.add(kv(ScmLogFields.CONTAINER_IMAGE_NAME, firstPresent(
                environment.getProperty("scm.runtime.image.name"),
                System.getenv("CONTAINER_IMAGE_NAME")
        )));
        arguments.add(kv(ScmLogFields.CONTAINER_IMAGE_TAG, firstPresent(
                environment.getProperty("scm.runtime.image.tag"),
                System.getenv("CONTAINER_IMAGE_TAG")
        )));
        arguments.add(kv(ScmLogFields.KUBERNETES_NAMESPACE, firstPresent(
                environment.getProperty("scm.runtime.kubernetes.namespace"),
                System.getenv("KUBERNETES_NAMESPACE")
        )));
        arguments.add(kv(ScmLogFields.KUBERNETES_POD_NAME, firstPresent(
                environment.getProperty("scm.runtime.kubernetes.pod-name"),
                System.getenv("KUBERNETES_POD_NAME"),
                System.getenv("HOSTNAME")
        )));
        arguments.add(kv(ScmLogFields.KUBERNETES_NODE_NAME, firstPresent(
                environment.getProperty("scm.runtime.kubernetes.node-name"),
                System.getenv("KUBERNETES_NODE_NAME")
        )));
        return arguments.toArray();
    }

    private static String deploymentEnvironment(Environment environment) {
        Set<String> allowed = Set.of("dev", "test", "pilot", "prod");
        for (String profile : environment.getActiveProfiles()) {
            if (allowed.contains(profile)) {
                return profile;
            }
        }
        String configured = firstPresent(environment.getProperty("deployment.environment"), environment.getProperty("spring.profiles.active"));
        return allowed.contains(configured) ? configured : "dev";
    }

    private static String runtime(Environment environment) {
        String configured = firstPresent(environment.getProperty("scm.runtime"), System.getenv("SCM_RUNTIME"));
        if ("kubernetes".equals(configured) || "standalone".equals(configured)) {
            return configured;
        }
        return firstPresent(System.getenv("KUBERNETES_SERVICE_HOST")) == null ? "standalone" : "kubernetes";
    }

    private static String implementationVersion() {
        Package packageInfo = ScmCacheInitLogging.class.getPackage();
        return packageInfo == null ? null : textOrNull(packageInfo.getImplementationVersion());
    }

    private static String firstPresent(String... values) {
        for (String value : values) {
            String text = textOrNull(value);
            if (text != null) {
                return text;
            }
        }
        return null;
    }

    private static String textOrNull(String value) {
        if (value == null || value.isBlank() || "-".equals(value.trim()) || "null".equalsIgnoreCase(value.trim())) {
            return null;
        }
        return value.trim();
    }
}
