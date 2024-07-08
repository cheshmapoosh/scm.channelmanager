package ir.daneshrefah.scm.process.service.dto.task;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TaskCompleteRequest {
    private String taskId;
    private String action;
    private JsonNode data;
}
