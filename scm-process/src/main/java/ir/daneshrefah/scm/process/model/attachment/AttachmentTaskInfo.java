package ir.daneshrefah.scm.process.model.attachment;

import ir.daneshrefah.scm.process.model.BaseProcessModel;
import lombok.Data;

import java.util.List;

@Data
public class AttachmentTaskInfo implements BaseProcessModel {
    private List<AttachmentInfo> attachmentInfos;
    private List<String> deletes;
}
