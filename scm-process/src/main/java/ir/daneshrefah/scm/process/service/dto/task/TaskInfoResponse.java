package ir.daneshrefah.scm.process.service.dto.task;

import ir.daneshrefah.scm.process.model.BaseProcessModel;
import ir.daneshrefah.scm.process.model.process.ProcessInstanceInfo;
import ir.daneshrefah.scm.process.model.task.Assignment;
import ir.daneshrefah.scm.process.model.task.TaskMetadata;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-15
 */
@Data
public class TaskInfoResponse implements BaseProcessModel {
    private String taskId;
    private String name;
    private List<Assignment> assignments;
    private Map<String,Object> data;
    private TaskMetadata metadata;
    private ProcessInstanceInfo processInstance;
    private Long createTime;
}
