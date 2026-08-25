package ir.daneshrefah.scm.provider.scm.registry;

import com.fasterxml.jackson.databind.JavaType;
import org.apache.camel.Processor;

import java.util.Objects;

public record ScmResourceActionDescriptor(
        String resourceName,
        String springBeanName,
        String actionName,
        String javaMethodName,
        JavaType inputType,
        JavaType outputType,
        Processor invocationDelegate
) {
    public ScmResourceActionDescriptor {
        Objects.requireNonNull(resourceName, "resourceName");
        Objects.requireNonNull(springBeanName, "springBeanName");
        Objects.requireNonNull(actionName, "actionName");
        Objects.requireNonNull(javaMethodName, "javaMethodName");
        Objects.requireNonNull(outputType, "outputType");
        Objects.requireNonNull(invocationDelegate, "invocationDelegate");
    }

    public boolean acceptsInput() {
        return inputType != null;
    }
}
