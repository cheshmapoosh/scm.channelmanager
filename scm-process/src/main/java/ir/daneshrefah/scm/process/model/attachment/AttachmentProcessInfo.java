package ir.daneshrefah.scm.process.model.attachment;

import ir.daneshrefah.scm.process.model.BaseProcessModel;
import lombok.Data;

import java.util.List;

@Data
public class AttachmentProcessInfo implements BaseProcessModel {
    private List<AttachmentInfo> attachmentInfos;
    private List<String> deletes;
    private List<String> roles;
    private List<String> permissions;
}
