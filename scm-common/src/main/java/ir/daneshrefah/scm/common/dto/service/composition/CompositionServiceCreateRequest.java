package ir.daneshrefah.scm.common.dto.service.composition;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.service.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompositionServiceCreateRequest implements RequestData {

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
    private Boolean checkAccessFirstAuthentication;
    @NotNull
    private Boolean checkAccessSecondAuthentication;
    @NotNull
    private Boolean checkAccessService;
    @NotNull
    private Boolean checkAccessAsset;
    private String amountProperty;
    private String assetProperty;
    @NotNull
    private ServiceCompositionType compositionType;

}
