package ir.daneshrefah.scm.common.model.operation;

import ir.daneshrefah.scm.common.AbstractAuditableModel;
import ir.daneshrefah.scm.common.model.protocol.ProtocolType;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for {@link ir.daneshrefah.scm.core.entity.operation.OperationProviderEntity}
 */
@Getter
@Setter
public class OperationProvider extends AbstractAuditableModel<String> {
    @Size(max = 36)
    private String id;
    @Size(max = 100)
    private String uri;
    private Boolean active;

}
