package ir.daneshrefah.scm.process.service.dto.history;

import ir.daneshrefah.scm.common.dto.PagedRequestData;
import lombok.Data;

@Data
public class HistoryProcessRequest extends PagedRequestData {
    private String assignee;//TODO remove this
    private Long fromDate;
    private Long toDate;
    private Boolean activeProcess;
}
