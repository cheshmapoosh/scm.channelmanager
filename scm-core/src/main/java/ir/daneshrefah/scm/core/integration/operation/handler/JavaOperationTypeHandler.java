package ir.daneshrefah.scm.core.integration.operation.handler;

import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.constant.OperationCode;
import ir.daneshrefah.scm.common.exception.ScmException;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.operation.OperationType;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JavaOperationTypeHandler implements OperationTypeHandler {

    private final ApplicationContext applicationContext;

    @Override
    public OperationType getOperationType() {
        return OperationType.JAVA;
    }

    @Override
    public void config(RouteDefinition route, Operation operation) {
        String operationCode = operation.getName();
        String beanName = operation.getPath();
        var bean = applicationContext.getBean(beanName);
        Method targetMethod = findTargetMethod(bean, operationCode);
        String targetBeanPath = createTargetBeanPath(targetMethod);
        findRequestBodyParameter(targetMethod).ifPresent(parameter -> {
            route.unmarshal().json(JsonLibrary.Jackson, parameter.getType());
        });
        route.bean("beanValidator");
        applyTargetMethod(route,beanName,targetMethod,targetBeanPath);
    }

    private void applyTargetMethod(RouteDefinition route, String beanName, Method targetMethod,String targetBeanPath) {
        if (hasExchangeInMethodInput(targetMethod)) {
            /*
             when you need camel 'Exchange' instance in your method input
             example:
             ** path variables and query params must be annotated with '@Header'
             ** api body dto must be annotated as '@Body'
             @JavaService(operationCode = SVC_ASSETS_FAVOURITE)
             public ResultDto process(Exchange exchange,
                    @Body Person person,
                    @Header("userId") String userId) {...}
             */
            route.bean(beanName, targetMethod.getName());
        }else {
            /*
               example:
                 ** path variables and query params automatically filed by their names.
                @JavaService(operationCode = SVC_ASSETS_FAVOURITE)
                public AccountFavoriteActivityResponse accountFavoriteActivity(AccountFavoriteActivityRequest request){...}
             */
            route.bean(beanName, targetBeanPath);
        }
    }



    private String createTargetBeanPath(Method targetMethod) {
        final StringBuilder beanPath = new StringBuilder(targetMethod.getName());
        beanPath.append("(");
        final String basePackage = this.getClass().getPackage().getName().split("\\.")[0];
        Arrays.stream(targetMethod.getParameters())
                .forEach(parameter -> {
                    if (parameter.getType().getPackage().getName().startsWith(basePackage)) {
                        //REQUEST BODY
                        beanPath.append("${body}");
                    } else {
                        //PATH VARIABLE OR QUERY PARAMS
                        beanPath.append("${header.").append(parameter.getName()).append("}");
                    }
                    beanPath.append(",");
                });
        beanPath.append(")");
        return StringUtils.removeEnd(beanPath.toString(), ",");
    }

    private Optional<Parameter> findRequestBodyParameter(Method targetMethod) {
        String projectBasePackageFirstPart = this.getClass().getPackage().getName().split("\\.")[0];
        return Arrays.stream(targetMethod.getParameters())
                .filter(p -> p.getType().getPackage().getName().startsWith(projectBasePackageFirstPart))
                .findFirst();
    }

    private boolean hasExchangeInMethodInput(Method targetMethod) {
        String camelExchangeClassPath = Exchange.class.getName();
        return Arrays.stream(targetMethod.getParameters())
                .anyMatch(p -> p.getType().getName().equalsIgnoreCase(camelExchangeClassPath));
    }

    private Method findTargetMethod(Object bean, String operationCode) {
        return Arrays.stream(ReflectionUtils
                        .getAllDeclaredMethods(bean.getClass()))
                .peek(method -> method.setAccessible(true))
                .filter(method -> Arrays.stream(method.getDeclaredAnnotations())
                        .filter(annotation -> annotation.annotationType().equals(JavaService.class))
                        .map(JavaService.class::cast)
                        .map(JavaService::operationCode)
                        .map(OperationCode::name)
                        .anyMatch(code -> code.equals(operationCode)))
                .findFirst().orElseThrow(() -> new ScmException("SCM.10000", "Java service method not found"));
    }
}
