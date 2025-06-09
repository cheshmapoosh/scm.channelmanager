package ir.daneshrefah.scm.core.integration.service;

import ir.daneshrefah.scm.core.integration.service.scanner.impl.JavaServiceMetadata;
import ir.daneshrefah.scm.plugin.api.exception.JavaServiceClassNotDefinedException;
import ir.daneshrefah.scm.plugin.api.exception.JavaServiceMethodNotFoundException;
import ir.daneshrefah.scm.plugin.api.exception.JavaServiceParameterClassNotFoundException;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.plugin.api.utils.ClassLoader;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-21
 */
public class JavaServiceFinder {

    public static MethodInfo findJavaServiceMethodInfo(String serviceCode) {
        JavaServiceMetadata metadata = getJavaServiceMetadata(serviceCode).orElseThrow(()->new RuntimeException("Java Service Metadata Could not found"));

        String classNameOrg = metadata.getJavaImplementationClassName();

        if (StringUtils.isEmpty(classNameOrg) || !classNameOrg.contains(".")) {
            return new MethodInfo(new JavaServiceClassNotDefinedException(metadata.getCode().name(), "invalid class name '" + classNameOrg + "'"));
        }
        if (!classNameOrg.contains("(") && !classNameOrg.contains(")")) {
            classNameOrg = classNameOrg + "()";
        }
        if (!classNameOrg.contains("(") || !classNameOrg.contains(")")) {
            return new MethodInfo(new JavaServiceClassNotDefinedException(metadata.getCode().name(), "invalid class name '" + classNameOrg + "'"));
        }

        classNameOrg = classNameOrg.trim().substring(0, classNameOrg.indexOf(')'));

        int lastDotIndex = classNameOrg.split("\\(")[0].lastIndexOf('.');
        String classNameString = classNameOrg.split("\\(")[0].substring(0, lastDotIndex);
        String methodNameString = classNameOrg.split("\\(")[0].substring(lastDotIndex + 1);
        String paramTypesString = classNameOrg.split("\\(").length > 1 ? classNameOrg.split("\\(")[1] : "";

        Class<?>[] paramTypes = null;
        if (StringUtils.isEmpty(paramTypesString)) {
            paramTypes = new Class<?>[0];
        } else {
            try {
                paramTypes = parseParamTypesFromStr(metadata, paramTypesString);
            } catch (ClassNotFoundException e) {
                return new MethodInfo(new JavaServiceClassNotDefinedException(metadata.getCode().name(),metadata.getJavaImplementationClassName(), e));
            }
        }

        AbstractJavaService javaServiceInstance = null;
        try {
            javaServiceInstance = ClassLoader.findBeanOrCreateInstanceOfClass(classNameString, AbstractJavaService.class);
        } catch (Exception e) {
            return new MethodInfo(new JavaServiceClassNotDefinedException(metadata.getCode().name(),metadata.getJavaImplementationClassName(), e));
        }
        if (null == javaServiceInstance) {
            return new MethodInfo(new JavaServiceClassNotDefinedException(metadata.getCode().name(), "could not found instance of class '" +
                    classNameString + "'"));
        }

        Method method;
        try {
            method = javaServiceInstance.getClass().getMethod(methodNameString, paramTypes);
            ir.daneshrefah.scm.common.annotation.JavaService javaServiceAnnotation = method.getAnnotation(ir.daneshrefah.scm.common.annotation.JavaService.class);
            if (Objects.isNull(javaServiceAnnotation)) {
                return new MethodInfo(new JavaServiceMethodNotFoundException(metadata.getCode().name(), null));
            }
            if (StringUtils.isNotEmptyAndNotEquals(javaServiceAnnotation.operationCode().name(), metadata.getCode().name())) {
                return new MethodInfo(new JavaServiceMethodNotFoundException(metadata.getCode().name(), null));
            }
        } catch (NoSuchMethodException e) {
            return new MethodInfo(new JavaServiceMethodNotFoundException(metadata.getCode().name(), e));
        }
        return new MethodInfo(javaServiceInstance, method, paramTypes);
    }

    private static Optional<JavaServiceMetadata> getJavaServiceMetadata(String serviceCode) {
//        return ClassContextCache.getInstance().get(ClassContextCache.Repository.JAVA_SERVICE_METADATA,serviceCode,JavaServiceMetadata.class);
        return null;
    }

    private static Class<?>[] parseParamTypesFromStr(JavaServiceMetadata metadata, String methodParameters) throws ClassNotFoundException {
        if (StringUtils.isEmpty(methodParameters)) {
            return new Class[0];
        }
        methodParameters = methodParameters.trim();
        if (StringUtils.endsWith(methodParameters, ")")) {
            methodParameters = methodParameters.substring(0, methodParameters.indexOf(')'));
        }
        if (StringUtils.isEmpty(methodParameters)) {
            return new Class[0];
        }
        String[] paramTypesStr = methodParameters.split(",");
        Class<?>[] paramTypes = new Class<?>[paramTypesStr.length];
        for (int i = 0; i < paramTypesStr.length; i++) {
            String paramTypeStr = paramTypesStr[i].trim();

            if ("java.lang.String".equalsIgnoreCase(paramTypesStr[i].trim())) {
                paramTypes[i] = String.class;
            } else if (!StringUtils.isBlank(paramTypeStr) && paramTypeStr.toLowerCase().contains("int")) {
                paramTypes[i] = int.class;
            } else if ("java.lang.Integer".equalsIgnoreCase(paramTypesStr[i].trim())) {
                paramTypes[i] = Integer.class;
            } else if ("java.lang.Long".equalsIgnoreCase(paramTypesStr[i].trim())) {
                paramTypes[i] = Long.class;
            } else if (!StringUtils.isBlank(paramTypeStr) && paramTypeStr.toLowerCase().contains("boolean")) {
                paramTypes[i] = boolean.class;
            } else if ("java.lang.Boolean".equalsIgnoreCase(paramTypesStr[i].trim())) {
                paramTypes[i] = Boolean.class;
            } else {
                try {
                    paramTypes[i] = Class.forName(paramTypesStr[i].trim());
                } catch (ClassNotFoundException e) {
                    throw new JavaServiceParameterClassNotFoundException(metadata.getCode().name(), paramTypesStr[i], e);
                }
            }
        }
        return paramTypes;
    }


    @Getter
    public static class MethodInfo {

        public MethodInfo(AbstractJavaService instance, Method method, Class<?>[] paramTypes) {
            this.instance = instance;
            this.method = method;
            this.paramTypes = paramTypes;
        }

        public MethodInfo(RuntimeException error) {
            this.error = error;
        }

        private AbstractJavaService instance;
        private Method method;
        private RuntimeException error;
        private Class<?>[] paramTypes;
    }

}
