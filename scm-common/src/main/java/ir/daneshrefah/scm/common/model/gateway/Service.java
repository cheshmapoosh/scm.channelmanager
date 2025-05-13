package ir.daneshrefah.scm.common.model.gateway;

import ir.daneshrefah.scm.common.AbstractModel;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO for {@link Service}
 */
@Getter
@Setter
public class Service extends AbstractModel<Short> {
    @NotNull
    private Boolean financial = false;
    @NotNull
    private Boolean publish = false;
    @NotNull
    private Boolean applySecondLevelAuthentication = false;
    @Size(max = 100)
    private String name;
    @Size(max = 50)
    private String code;
    @Size(max = 3)
    private String abbreviation;
    private ServiceCategory serviceCategory;
    private List<ServiceOperation> serviceOperations;
    @Size(max = 20)
    private RoutingStrategy routingStrategy;
    @Size(max = 2048)
    private String metadata;


}