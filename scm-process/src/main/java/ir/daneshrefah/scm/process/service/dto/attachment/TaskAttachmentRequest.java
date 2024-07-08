package ir.daneshrefah.scm.process.service.dto.attachment;

import lombok.Data;

import java.util.Map;

@Data
public class TaskAttachmentRequest {
    private String taskId;
    private String assignee;
    private Map<String,Object> data;
}
