package ir.daneshrefah.scm.common.dto;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.service.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-24
 */
@Data
public class ServiceInfoRequest implements RequestData {

    @NotNull
    @NotBlank
    private String code;
    @NotNull
    @NotBlank
    private String title;
    private String alias;
    private Integer version;
    @NotNull
    private ServiceType type;
    @NotNull
    private ServiceStatus status;
    private String parentId;
    @NotNull
    private ServiceImplementationType implementationType;
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
    private String amountProperty;
    private String assetProperty;
    /**
     * this property used when implementationType is rest external
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
    /**
     * this property used in proxy services
     */
    private String proxyTargetServiceId;

}
