package ir.daneshrefah.scm.common.dto.serviceOperation;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ServiceOperationCreateRequest {

    @NotNull
    private Short serviceId;
    @NotNull
    private RoutingStrategy routingStrategy;
    @NotEmpty
    private List<String> operationIds;
    @NotNull
    private Boolean active;
    @NotBlank
    private String definitionId;
}
