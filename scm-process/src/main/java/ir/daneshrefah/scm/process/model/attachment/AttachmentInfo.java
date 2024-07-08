package ir.daneshrefah.scm.process.model.attachment;

import ir.daneshrefah.scm.process.model.BaseProcessModel;
import lombok.Data;

@Data
public class AttachmentInfo implements BaseProcessModel {
    private String attachmentName;
    private String type;
}

