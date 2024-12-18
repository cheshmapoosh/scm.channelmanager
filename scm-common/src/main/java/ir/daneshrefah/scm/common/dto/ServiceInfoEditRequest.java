package ir.daneshrefah.scm.common.dto;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.service.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ServiceInfoEditRequest implements RequestData {
    @NotNull
    @NotBlank
    private String id;
    @NotNull
    private LocalDateTime lastEditDate;
    @NotNull
    @NotBlank
    private String code;
    private String title;
    private String alias;
    private Integer version;
    private String metadata;
    @NotNull
    private ServiceType type;
    @NotNull
    private ServiceStatus status;
    private String parentId;
    private String requestJsonSchema;
    private String responseJsonSchema;
    @NotNull
    private Boolean checkAccessFirstAuthentication;
    @NotNull
    private Boolean checkAccessSecondAuthentication;
    @NotNull
    private Boolean checkAccessService;
    @NotNull
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
