package ir.daneshrefah.scm.process.service.dto.processDefinition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessDeployResponse {
    private String id;
    private String name;
    private Date deploymentDate;
}
