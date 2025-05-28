package ir.daneshrefah.scm.common.service;

import ir.daneshrefah.scm.common.dto.service.java.JavaServiceCreateRequest;
import ir.daneshrefah.scm.common.dto.service.java.JavaServiceEditRequest;
import ir.daneshrefah.scm.common.model.service.ScmService;

public interface JavaServicesService {
    ScmService createJavaService(JavaServiceCreateRequest request);

    ScmService editJavaService(JavaServiceEditRequest request);

    ScmService getJavaService(String javaServiceId);
}
