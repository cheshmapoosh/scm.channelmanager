package ir.daneshrefah.scm.process.input;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.process.service.attachment.AttachmentService;
import ir.daneshrefah.scm.process.service.dto.attachment.ProcessAttachmentRequest;
import ir.daneshrefah.scm.process.service.dto.attachment.ProcessDeleteAttachmentRequest;
import ir.daneshrefah.scm.process.service.dto.attachment.TaskAttachmentRequest;
import ir.daneshrefah.scm.process.service.dto.attachment.TaskDeleteAttachmentRequest;
import org.springframework.stereotype.Service;

import static ir.daneshrefah.scm.common.constant.ServiceCode.*;

@Service
public class AttachmentManagementService extends AbstractJavaService {

    private final AttachmentService attachmentService;

    public AttachmentManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, AttachmentService attachmentService) {
        super(producerTemplate, objectMapper);
        this.attachmentService = attachmentService;
    }

    @JavaService(serviceCode = SVC_ATTACHMENT_TASK)
    public void taskAttachment(TaskAttachmentRequest taskAttachmentRequest) throws Exception {
        attachmentService.taskAttachment(taskAttachmentRequest);
    }

    @JavaService(serviceCode = SVC_ATTACHMENT_DELETE_TASK)
    public void deleteTaskAttachment(TaskDeleteAttachmentRequest taskDeleteAttachmentRequest) throws Exception {
        attachmentService.deleteTaskAttachment(taskDeleteAttachmentRequest);
    }

    @JavaService(serviceCode = SVC_ATTACHMENT_PROCESS)
    public void processAttachment(ProcessAttachmentRequest processAttachmentRequest) throws Exception {
        attachmentService.processAttachment(processAttachmentRequest);
    }

    @JavaService(serviceCode = SVC_ATTACHMENT_DELETE_PROCESS)
    public void deleteProcessAttachment(ProcessDeleteAttachmentRequest processDeleteAttachmentRequest) throws Exception {
        attachmentService.deleteProcessAttachment(processDeleteAttachmentRequest);
    }
}
