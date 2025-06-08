package ir.daneshrefah.scm.common.model.error;

import lombok.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SCMFault {
    private String status;
    private String title;
    private URI source;
    @Singular("detail")
    private Map<String, Object> details;
    private FaultMessage faultMessage;
    @Singular("cause")
    private List<SCMFault> causes;

}
