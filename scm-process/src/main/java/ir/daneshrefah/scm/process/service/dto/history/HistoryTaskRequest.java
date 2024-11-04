package ir.daneshrefah.scm.process.service.dto.history;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HistoryTaskRequest extends PagedRequestData {
    private String processInstanceId;
    private String nationalCode;
    private String userVariable;
    private boolean activeProcess;
    private boolean isIncludePersonInfo;

}
