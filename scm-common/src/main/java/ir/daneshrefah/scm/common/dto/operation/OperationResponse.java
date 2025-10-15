package ir.daneshrefah.scm.common.dto.operation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ir.daneshrefah.scm.common.model.operation.OperationType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OperationResponse {
    private String id;
    private String title;
    private String name;
    private String path;
    private Boolean active;
    private OperationType type;
    private String operationProviderTitle;
    private String operationProviderName;
    @JsonIgnore
    private String version;
}
