package ir.daneshrefah.scm.common.model.operation;

import ir.daneshrefah.scm.common.AbstractAuditableModel;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO for {@link ir.daneshrefah.scm.core.entity.operation.OperationEntity}
 */
@Getter
@Setter
public class Operation extends AbstractAuditableModel<String> {
    @Size(max = 36)
    private String id;

    @Size(max = 100)
    private String title;

    @Size(max = 50)
    private String name;

    @Size(max = 100)
    private String path;

    @NotNull
    private Boolean active = false;

    @Size(max = 255)
    private String description;

    private OperationType type;

    private OperationProvider provider;

    private List<OperationDefinition> definitions;

}