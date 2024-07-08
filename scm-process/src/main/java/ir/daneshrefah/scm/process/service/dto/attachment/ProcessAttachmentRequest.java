package ir.daneshrefah.scm.process.service.dto.attachment;

import lombok.Data;

import java.util.Map;

@Data
public class ProcessAttachmentRequest {
     private String processId;
     private Map<String,Object> data;
}
