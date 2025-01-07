package ir.daneshrefah.scm.core.integration.service.scanner.impl;

import ir.daneshrefah.scm.common.constant.ServiceCode;
import ir.daneshrefah.scm.common.model.service.ServiceType;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.List;

@Data
public class JavaServiceMetadata {
    private boolean springBean;
    /* PARENT BASIC INFORMATION */
    private ServiceCode parentCode;
    /* BASIC INFORMATION */
    private ServiceCode code;
    private String title;
    private String alias;
    private ServiceType type;
    private String javaImplementationClassName;
    /* SPRING BASED CONTEXT */
    private String implementationBean;
    /* DEFAULT BASED CONTEXT */
    private String implementationClassName;
    /* REFLECT CACHED FLAG */
    private Method method;
    private List<Class<?>> methodParameterTypes;
    /* SECURITY CHECKS */
    private Boolean checkAccessFirstAuthentication;
    private Boolean checkAccessSecondAuthentication;
    private Boolean checkAccessService;
    private Boolean checkAccessAsset;

}
