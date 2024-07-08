package ir.daneshrefah.scm.process.service.dto.attachment;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TaskDeleteAttachmentRequest {
    private String taskId;
    private String assignee;
    private List<String> deletes;
}
