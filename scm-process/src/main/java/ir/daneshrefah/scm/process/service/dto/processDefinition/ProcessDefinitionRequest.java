package ir.daneshrefah.scm.process.service.dto.processDefinition;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import lombok.Data;

@Data
public class ProcessDefinitionRequest extends PagedRequestData {
    private boolean includeActiveCount = false;
}
