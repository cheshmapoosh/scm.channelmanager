package ir.daneshrefah.scm.core.integration.operation.handlers.java;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.core.integration.service.JavaServiceFinder;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
@RequiredArgsConstructor
public class JavaOperationProcessor implements Processor {

    private final ObjectMapper objectMapper;

    @Override
    public void process(Exchange exchange) throws Exception {
        Operation operation = exchange.getProperty(Message.OPERATION, Operation.class);
        String jsonBody = exchange.getMessage().getBody(String.class);
        JavaServiceFinder.MethodInfo methodInfo = JavaServiceFinder.findJavaServiceMethodInfo(operation.getName());
        AbstractJavaService instance = methodInfo.getInstance();
        Method method = methodInfo.getMethod();
        Class<?>[] parameterTypes = method.getParameterTypes();
        var requestBody = objectMapper.readValue(jsonBody, parameterTypes[parameterTypes.length - 1]);
        method.invoke(instance, requestBody);
    }
}
