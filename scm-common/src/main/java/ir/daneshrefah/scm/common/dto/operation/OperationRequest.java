package ir.daneshrefah.scm.common.dto.operation;

import ir.daneshrefah.scm.common.dto.definition.DefinitionRequest;
import ir.daneshrefah.scm.common.model.operation.OperationType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OperationRequest {
    private String id;
    @NotEmpty
    private String title;
    @NotEmpty
    private String name;
    private String path;
    @NotNull
    private Boolean active;
    @NotNull
    private OperationType type;
    private String description;
    @NotEmpty
    private String operationProviderId;
    private DefinitionRequest definition;
}
