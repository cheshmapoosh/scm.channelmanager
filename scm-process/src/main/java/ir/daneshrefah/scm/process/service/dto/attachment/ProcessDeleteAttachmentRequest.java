package ir.daneshrefah.scm.process.service.dto.attachment;

import lombok.Data;

import java.util.List;

@Data
public class ProcessDeleteAttachmentRequest {
    private String processID;
    private String assignee;
    private List<String> deletes;
}
