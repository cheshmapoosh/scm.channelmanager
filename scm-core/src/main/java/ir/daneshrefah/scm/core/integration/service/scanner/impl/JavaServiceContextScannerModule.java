package ir.daneshrefah.scm.core.integration.service.scanner.impl;

import io.github.classgraph.*;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.core.integration.service.scanner.spec.ClassContextCache;
import ir.daneshrefah.scm.core.integration.service.scanner.spec.ContextScannerModule;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

@Component
@Slf4j
public class JavaServiceContextScannerModule implements ContextScannerModule {

    @Override
    public void register(Map<String, ClassInfo> classInfoRepository) {
        classInfoRepository.forEach((key, value) -> {
            MethodInfoList declaredMethodInfo = value.getDeclaredMethodInfo();
            for (MethodInfo methodInfo : declaredMethodInfo) {
                AnnotationInfoList annotationInfo = methodInfo.getAnnotationInfo();
                for (AnnotationInfo info : annotationInfo) {
                    if (info.getName().contains("JavaService")) {
                        loadJavaServiceMetadata(value);
                    }
                }
            }
        });
    }

    private void loadJavaServiceMetadata(ClassInfo value) {
        Class<?> aClass = value.loadClass();
        ClassContextCache.getInstance().put(ClassContextCache.Repository.CLASSES, aClass.getName(), aClass);
        Optional<String> springBeanNameOptional = getSpringBeanName(aClass);
        for (Method method : aClass.getDeclaredMethods()) {
            JavaServiceMetadata metadata = createJavaServiceMetadata(method, aClass, springBeanNameOptional);
            if (Objects.nonNull(metadata)) {
                ClassContextCache.getInstance().put(ClassContextCache.Repository.JAVA_SERVICE_METADATA, metadata.getCode().name(), metadata);
                log.info(">>> JavaService [{}] successfully loaded ", metadata.getCode());
            }
        }
    }

    private JavaServiceMetadata createJavaServiceMetadata(Method method, Class<?> aClass, Optional<String> springBeanNameOptional) {
        try {
            method.setAccessible(true);
            JavaService javaServiceAnnotation = method.getDeclaredAnnotation(JavaService.class);
            JavaServiceMetadata metadata = new JavaServiceMetadata();
            metadata.setSpringBean(springBeanNameOptional.isPresent());
            metadata.setImplementationBean(springBeanNameOptional.orElse(null));
            if (!javaServiceAnnotation.serviceCode().getType().equals(ServiceImplementationType.JAVA)){
                log.error(">>> SERVICE CODE '{}' MUST HAVE JAVA IMPL TYPE", javaServiceAnnotation.serviceCode().name());
                throw new RuntimeException("MUST HAVE JAVA IMPL TYPE");
            }
            metadata.setCode(javaServiceAnnotation.serviceCode());
            metadata.setImplementationClassName(aClass.getName());
            metadata.setMethod(method);
            metadata.setMethodParameterTypes(getMethodInputParameterTypes(method));
            metadata.setJavaImplementationClassName(generateJavaImplementationClassName(metadata));
            return metadata;
        } catch (Exception ignore) {
            return null;
        }
    }

    private String generateJavaImplementationClassName(JavaServiceMetadata metadata) {
        final String paramSign = "{p}";
        final String MethodParamSign = "(" + paramSign + ")";
        String javaImplClassName = "";
        String implementationBean = metadata.getImplementationBean();
        String implementationClassName = metadata.getImplementationClassName();
        List<Class<?>> methodParameterTypes = metadata.getMethodParameterTypes();
        if (metadata.isSpringBean()) {
            javaImplClassName += "bean:" + implementationBean +"."+metadata.getMethod().getName()+ MethodParamSign;
        } else {
            javaImplClassName += implementationClassName + "."+metadata.getMethod().getName()+ MethodParamSign;
        }
        String parameterDefinition = "";
        if (Objects.nonNull(methodParameterTypes)) {
            for (Class<?> parameterType : methodParameterTypes) {
                parameterDefinition = parameterType.getName() + ",";
            }
            parameterDefinition = org.apache.commons.lang3.StringUtils.removeEnd(parameterDefinition, ",");
        }
        return javaImplClassName.replace(paramSign, parameterDefinition);
    }


    private List<Class<?>> getMethodInputParameterTypes(Method method) {
        return new ArrayList<>(Arrays.asList(method.getParameterTypes()));
    }

    private Optional<String> getSpringBeanName(Class<?> aClass) {
        List<Class<? extends Annotation>> springBeanAnnotations =
                Arrays.asList(Component.class, Service.class, Repository.class, Controller.class);
        return Arrays.stream(aClass.getDeclaredAnnotations())
                .toList()
                .stream()
                .filter(annotation -> springBeanAnnotations.contains(annotation.annotationType()))
                .map(annotation -> {
                    String componentName = StringUtils.EMPTY;
                    if (annotation instanceof Component component) {
                        componentName = component.value();
                    } else if (annotation instanceof Service service) {
                        componentName = service.value();
                    } else if (annotation instanceof Repository repository) {
                        componentName = repository.value();
                    } else if (annotation instanceof Controller controller) {
                        componentName = controller.value();
                    }
                    if (StringUtils.isBlank(componentName)) {
                        String simpleName = aClass.getSimpleName();
                        componentName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
                    }
                    return componentName;
                }).findFirst();
    }
}
