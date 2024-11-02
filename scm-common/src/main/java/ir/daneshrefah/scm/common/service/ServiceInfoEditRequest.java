package ir.daneshrefah.scm.common.service;

import ir.daneshrefah.scm.common.dto.RequestData;
import ir.daneshrefah.scm.common.model.service.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ServiceInfoEditRequest implements RequestData {
    private String id;
    private LocalDateTime lastEditDate;
    private String code;
    private String title;
    private String alias;
    private Integer version;
    private String metadata;
    private ServiceType type;
    private ServiceStatus status;
    private String parentId;
    private String requestJsonSchema;
    private String responseJsonSchema;
    private Boolean checkAccessFirstAuthentication;
    private Boolean checkAccessSecondAuthentication;
    private Boolean checkAccessService;
    private Boolean checkAccessAsset;
    private String customerProperty;
    private String amountProperty;
    private String assetProperty;
    /**
     * this property used when implementationType is java
     * */
    private String javaImplementationClassName;
    /**
     * this property used when implementationType is external
     * */
    private String serviceProviderId;
    private String path;
    private HttpMethod httpMethod;
    private HttpContentType requestContentType;
    private ExternalServiceBodyType requestBodyType;
    /**
     * this property used when implementationType is composite
     */
    private ServiceCompositionType compositionType;
}
