package ir.daneshrefah.scm.common.model.gateway;

import ir.daneshrefah.scm.common.AbstractAuditableModel;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for {@link RouteConsumerEntity}
 */
@Getter
@Setter
public class ServiceOperation extends AbstractAuditableModel<String> {
    @Size(max = 36)
    private String id;
    @NotNull
    private Boolean active;
    @Size(max = 50)
    private String operationCode;
    @Size(max = 2048)
    private String metadata;
}