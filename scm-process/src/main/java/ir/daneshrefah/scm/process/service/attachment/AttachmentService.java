package ir.daneshrefah.scm.process.service.attachment;

import com.fasterxml.jackson.core.JsonProcessingException;
import ir.daneshrefah.scm.process.service.dto.attachment.ProcessDeleteAttachmentRequest;
import ir.daneshrefah.scm.process.service.dto.attachment.TaskDeleteAttachmentRequest;
import ir.daneshrefah.scm.process.service.dto.attachment.ProcessAttachmentRequest;
import ir.daneshrefah.scm.process.service.dto.attachment.TaskAttachmentRequest;

public interface AttachmentService {
    void taskAttachment(TaskAttachmentRequest taskAttachmentRequest) throws Exception;

    void deleteTaskAttachment(TaskDeleteAttachmentRequest taskDeleteAttachmentRequest) throws Exception;

    void processAttachment(ProcessAttachmentRequest processAttachmentRequest) throws Exception;

     void deleteProcessAttachment(ProcessDeleteAttachmentRequest processDeleteAttachmentRequest) throws Exception;
}
