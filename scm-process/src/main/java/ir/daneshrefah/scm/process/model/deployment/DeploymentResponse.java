package ir.daneshrefah.scm.process.model.deployment;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class DeploymentResponse {
    private String id;
    private String name;
    private Date deploymentTime;
    private String source;
    private String tenantId;
}
