package ir.daneshrefah.scm.process.model.task;

import ir.daneshrefah.scm.process.model.BaseProcessModel;
import ir.daneshrefah.scm.process.model.process.ProcessInstanceInfo;
import lombok.Data;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-15
 */
@Data
public class TaskInfo implements BaseProcessModel {

    private String taskId;
    private String name;
    private List<Assignment> assignments;
    private TaskMetadata metadata;
    private ProcessInstanceInfo processInstance;

//    private String taskPersianName;
//    private String taskDescription;
//    private String taskDefinitionKey;
//    private String description;
//    private List<Assignment> assignments;
//    private Date createTime;
//    private Long createTimeMillis;
//    private UserTaskStatus userTaskStatus;
//    private ProcessResponse process;
//    private Map<String,Object> data;

}
