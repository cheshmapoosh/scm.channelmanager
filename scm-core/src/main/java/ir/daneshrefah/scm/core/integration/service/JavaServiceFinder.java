package ir.daneshrefah.scm.core.integration.service;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.plugin.api.exception.JavaServiceClassNotDefinedException;
import ir.daneshrefah.scm.plugin.api.exception.JavaServiceMethodNotFoundException;
import ir.daneshrefah.scm.plugin.api.exception.JavaServiceParameterClassNotFoundException;
import ir.daneshrefah.scm.plugin.api.model.service.java.JavaService;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.plugin.api.utils.ClassLoader;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.Getter;

import java.lang.reflect.Method;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-21
 */
public class JavaServiceFinder {

    public static MethodInfo findJavaServiceMethodInfo(JavaService service) {
        String classNameOrg = service.getJavaImplementationClassName();

        if (StringUtils.isEmpty(classNameOrg) || !classNameOrg.contains(".")) {
            return new MethodInfo(new JavaServiceClassNotDefinedException(service, "invalid class name '" + classNameOrg + "'"));
        }
        if (!classNameOrg.contains("(") && !classNameOrg.contains(")")) {
            classNameOrg = classNameOrg + "()";
        }
        if (!classNameOrg.contains("(") || !classNameOrg.contains(")")) {
            return new MethodInfo(new JavaServiceClassNotDefinedException(service, "invalid class name '" + classNameOrg + "'"));
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
                paramTypes = parseParamTypesFromStr(service, paramTypesString);
            } catch (ClassNotFoundException e) {
                return new MethodInfo(new JavaServiceClassNotDefinedException(service, e));
            }
        }

        AbstractJavaService javaServiceInstance = null;
        try {
            javaServiceInstance = ClassLoader.findBeanOrCreateInstanceOfClass(classNameString, AbstractJavaService.class);
        } catch (Exception e) {
            return new MethodInfo(new JavaServiceClassNotDefinedException(service, e));
        }
        if (null == javaServiceInstance) {
            return new MethodInfo(new JavaServiceClassNotDefinedException(service, "could not found instance of class '" +
                    classNameString + "'"));
        }

        Method method;
        try {
            method = javaServiceInstance.getClass().getMethod(methodNameString, paramTypes);
        } catch (NoSuchMethodException e) {
            return new MethodInfo(new JavaServiceMethodNotFoundException(service, e));
        }
        MethodInfo result = new MethodInfo(javaServiceInstance, method, paramTypes);
        return result;
    }

    private static Class<?>[] parseParamTypesFromStr(JavaService service, String methodParameters) throws ClassNotFoundException {
        if (StringUtils.isEmpty(methodParameters)) {
            return new Class[0];
        }
//        if (!methodParameters.contains("(")) {
//            return new Class<?>[]{Message.class, Service.class, Object.class};
//        }
//        methodParameters = methodParameters.split("\\(")[0];
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
            if ("String".equalsIgnoreCase(paramTypesStr[i].trim())) {
                paramTypes[i] = String.class;
            } else if ("int".equalsIgnoreCase(paramTypesStr[i].trim())) {
                paramTypes[i] = int.class;
            } else if ("Integer".equalsIgnoreCase(paramTypesStr[i].trim())) {
                paramTypes[i] = Integer.class;
            } else if ("Long".equalsIgnoreCase(paramTypesStr[i].trim())) {
                paramTypes[i] = Long.class;
            } else if ("Message".equalsIgnoreCase(paramTypesStr[i].trim())) {
                paramTypes[i] = Message.class;
            } else if ("Service".equalsIgnoreCase(paramTypesStr[i].trim())) {
                paramTypes[i] = Service.class;
            } else if ("boolean".equals(paramTypesStr[i].trim())) {
                paramTypes[i] = boolean.class;
            } else if ("Boolean".equalsIgnoreCase(paramTypesStr[i].trim())) {
                paramTypes[i] = Boolean.class;
            } else {
                try {
                    paramTypes[i] = Class.forName(paramTypesStr[i].trim());
                } catch (ClassNotFoundException e) {
                    throw new JavaServiceParameterClassNotFoundException(service, paramTypesStr[i], e);
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
