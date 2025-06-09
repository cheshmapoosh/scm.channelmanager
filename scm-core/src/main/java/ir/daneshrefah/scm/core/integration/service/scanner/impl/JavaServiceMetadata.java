package ir.daneshrefah.scm.core.integration.service.scanner.impl;

import ir.daneshrefah.scm.common.constant.OperationCode;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.List;

@Data
public class JavaServiceMetadata {
    private boolean springBean;
    /* BASIC INFORMATION */
    private OperationCode code;
    private String javaImplementationClassName;
    /* SPRING BASED CONTEXT */
    private String implementationBean;
    /* DEFAULT BASED CONTEXT */
    private String implementationClassName;
    /* REFLECT CACHED FLAG */
    private Method method;
    private List<Class<?>> methodParameterTypes;
}
