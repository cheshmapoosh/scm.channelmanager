package ir.daneshrefah.scm.process.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HistoryRequest {
    private String rootProcessInstanceId;
    private String nationalCode;
    private String userVariable;
    private boolean activeProcess;
}
