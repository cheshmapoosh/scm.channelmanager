package ir.daneshrefah.scm.provider.scm.registry;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record ScmResourceDescriptor(
        String resourceName,
        String springBeanName,
        Class<?> targetType,
        Map<String, ScmResourceActionDescriptor> actions
) {
    public ScmResourceDescriptor {
        Objects.requireNonNull(resourceName, "resourceName");
        Objects.requireNonNull(springBeanName, "springBeanName");
        Objects.requireNonNull(targetType, "targetType");
        actions = Collections.unmodifiableMap(new LinkedHashMap<>(actions));
    }
}
