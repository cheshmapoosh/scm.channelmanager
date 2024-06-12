package ir.daneshrefah.scm.process.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TaskRequest {
    private String taskId;
    private String nationalCode; //Remove this
    private String action;
}
