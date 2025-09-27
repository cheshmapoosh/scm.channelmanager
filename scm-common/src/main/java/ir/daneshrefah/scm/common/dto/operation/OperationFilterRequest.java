package ir.daneshrefah.scm.common.dto.operation;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import ir.daneshrefah.scm.common.model.operation.OperationType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OperationFilterRequest extends PagedRequestData {
    private String title;
    private String name;
    private String path;
    private Boolean active;
    private OperationType type;
}
