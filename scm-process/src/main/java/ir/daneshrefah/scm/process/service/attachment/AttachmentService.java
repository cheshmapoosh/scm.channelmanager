package ir.daneshrefah.scm.process.service.attachment;

import ir.daneshrefah.scm.process.service.dto.attachment.ProcessAttachmentRequest;
import ir.daneshrefah.scm.process.service.dto.attachment.ProcessDeleteAttachmentRequest;
import ir.daneshrefah.scm.process.service.dto.attachment.TaskAttachmentRequest;
import ir.daneshrefah.scm.process.service.dto.attachment.TaskDeleteAttachmentRequest;

public interface AttachmentService {
    void taskAttachment(TaskAttachmentRequest taskAttachmentRequest) throws Exception;

    void deleteTaskAttachment(TaskDeleteAttachmentRequest taskDeleteAttachmentRequest) throws Exception;

    void processAttachment(ProcessAttachmentRequest processAttachmentRequest) throws Exception;

     void deleteProcessAttachment(ProcessDeleteAttachmentRequest processDeleteAttachmentRequest) throws Exception;
}
