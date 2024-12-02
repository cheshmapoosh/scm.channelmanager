package ir.daneshrefah.scm.common.dto.provider;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.service.ServiceProviderProtocol;
import ir.daneshrefah.scm.common.model.service.ServiceProviderStatus;
import ir.daneshrefah.scm.common.validation.NotBlankIfPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ServiceProviderCreteRequest implements RequestData {

    @NotBlank
    private String code;
    @NotBlank
    private String title;
    @NotBlankIfPresent
    private String providerClassName;
    @NotNull
    private ServiceProviderProtocol protocol;
    @NotNull
    private ServiceProviderStatus status;
    @NotBlankIfPresent
    private String assetProviderId;
    //METADATA HANDLED BY PARAMETER

}
