package ir.daneshrefah.scm.common.model.error;

import ir.daneshrefah.scm.common.model.message.MessageStatus;
import lombok.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScmFault {
    // TODO TEMPORARY FOR SAVING CURRENT STATUS
    private MessageStatus status;
    private List<Error> errors;
    //
    private String title;
    private URI instance;
    @Singular("detail")
    private Map<String, Object> details;
    private FaultMessage faultMessage;
    @Singular("cause")
    private List<ScmFault> causes;

}
