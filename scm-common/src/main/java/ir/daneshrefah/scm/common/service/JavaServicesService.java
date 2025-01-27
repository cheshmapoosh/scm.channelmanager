package ir.daneshrefah.scm.common.service;

import ir.daneshrefah.scm.common.dto.service.java.JavaServiceCreateRequest;
import ir.daneshrefah.scm.common.dto.service.java.JavaServiceEditRequest;
import ir.daneshrefah.scm.common.model.service.Service;

public interface JavaServicesService {
    Service createJavaService(JavaServiceCreateRequest request);

    Service editJavaService(JavaServiceEditRequest request);

    Service getJavaService(String javaServiceId);
}
