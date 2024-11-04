package ir.daneshrefah.scm.process.service.dto.processInstance;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import lombok.Data;

@Data
public class ProcessInstanceRequest extends PagedRequestData {
    private String definitionId;
    private Long count;
}
