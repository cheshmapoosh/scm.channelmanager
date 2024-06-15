package ir.daneshrefah.scm.process.service.dto;

import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-15
 */
@Data
public class TaskFindRequest {

    private String taskId;
    private String assignee;
    private boolean includeMetadata;
    private boolean includePersonInfo;

}
