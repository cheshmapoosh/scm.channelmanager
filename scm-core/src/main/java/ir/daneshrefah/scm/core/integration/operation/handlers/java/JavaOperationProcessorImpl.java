package ir.daneshrefah.scm.core.integration.operation.handlers.java;

import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.core.integration.service.JavaServiceFinder;
import ir.daneshrefah.scm.core.integration.service.scanner.impl.JavaServiceMetadata;
import org.springframework.stereotype.Component;

@Component("javaOperationExecutor")
public class JavaOperationProcessorImpl implements JavaOperationExecutor {
    public JavaOperationProcessorImpl() {
        System.out.println("JavaOperationProcessorImpl");
    }

    @Override
    public void process(Operation operation) {

        JavaServiceMetadata metadata = JavaServiceFinder.getJavaServiceMetadata(operation.getName()).orElseThrow();

        JavaServiceFinder.MethodInfo javaServiceMethodInfo = JavaServiceFinder.findJavaServiceMethodInfo(operation.getName());

    }
}
