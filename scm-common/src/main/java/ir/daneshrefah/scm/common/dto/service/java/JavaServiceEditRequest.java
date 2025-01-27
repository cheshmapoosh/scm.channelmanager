package ir.daneshrefah.scm.common.dto.service.java;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.service.ServiceStatus;
import ir.daneshrefah.scm.common.model.service.ServiceType;
import ir.daneshrefah.scm.common.validation.NotBlankIfPresent;
import ir.daneshrefah.scm.common.validation.Numeric;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JavaServiceEditRequest implements RequestData {

    @NotNull
    @NotBlank
    private String id;
    @NotNull
    @NotBlank
    private String title;
    private String alias;
    @NotNull
    @Numeric
    private Integer version;
    @NotNull
    private ServiceType type;
    @NotNull
    private ServiceStatus status;
    @NotBlankIfPresent
    private String parentId;
    @NotNull
    private Boolean checkAccessFirstAuthentication;
    @NotNull
    private Boolean checkAccessSecondAuthentication;
    @NotNull
    private Boolean checkAccessService;
    @NotNull
    private Boolean checkAccessAsset;

}
