package ir.daneshrefah.scm.core.integration.operation.handler;

import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.operation.OperationType;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Processor;
import org.apache.camel.model.RouteDefinition;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;

@Component
@RequiredArgsConstructor
public class JavaOperationTypeHandler implements OperationTypeHandler {
    @Override
    public OperationType getOperationType() {
        return OperationType.JAVA;
    }

    @Override
    public void config(RouteDefinition route, Operation operation) {
        try {
            Class<?> processorType = ClassUtils.getClass(operation.getPath());
            if (processorType.isAssignableFrom(Processor.class)) {
                throw new RuntimeException("The type %s not instance of %s".formatted(operation.getPath(), Processor.class.getName()));
            }
            Processor processor = (Processor) ConstructorUtils.invokeConstructor(processorType);
            route.process(processor);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
}
