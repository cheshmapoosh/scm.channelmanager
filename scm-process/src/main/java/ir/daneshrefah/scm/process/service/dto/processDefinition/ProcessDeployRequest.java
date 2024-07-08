package ir.daneshrefah.scm.process.service.dto.processDefinition;

import lombok.Data;

@Data
public class ProcessDeployRequest {
    private String deploymentName;
    private String xmlBPMN;
}
