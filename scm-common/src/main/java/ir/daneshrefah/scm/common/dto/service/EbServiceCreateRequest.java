package ir.daneshrefah.scm.common.dto.service;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.validation.NotBlankIfPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EbServiceCreateRequest {
    private Integer id;
    @NotNull
    private Boolean publish;
    @NotBlankIfPresent
    private String name;
    @NotBlankIfPresent
    private String code;
    @NotBlankIfPresent
    private String abbreviation;
    @NotNull
    private Integer serviceCategoryId;
    @NotNull
    private Boolean financial;
    private RoutingStrategy routingStrategy;
    @NotNull
    private Boolean applySecondLevelAuthentication;
    @NotNull
    private Boolean applyAccountAuthorization;
    private ServiceCategoryRequest serviceCategory;
}
