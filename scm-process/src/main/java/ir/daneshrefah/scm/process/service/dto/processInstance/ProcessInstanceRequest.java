package ir.daneshrefah.scm.process.service.dto.processInstance;

import ir.daneshrefah.scm.common.dto.PagedRequestData;
import lombok.Data;

@Data
public class ProcessInstanceRequest extends PagedRequestData {
    private String deploymentId;
}
