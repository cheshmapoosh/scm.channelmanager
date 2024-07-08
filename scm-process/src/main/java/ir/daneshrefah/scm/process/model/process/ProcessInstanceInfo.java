package ir.daneshrefah.scm.process.model.process;

import ir.daneshrefah.scm.process.model.BaseProcessModel;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-15
 */
@Data

public class ProcessInstanceInfo implements BaseProcessModel {
    private String processInstanceId;
    private String processDefinitionId;
}
