package ir.daneshrefah.scm.process.service.dto.history;

import ir.daneshrefah.scm.common.dto.PagedRequestData;
import ir.daneshrefah.scm.process.model.filter.Filter;
import lombok.Data;

import java.util.List;

@Data
public class HistoryProcessRequest extends PagedRequestData {
    private String assignee;//TODO remove this
    private Long fromDate;
    private Long toDate;
    private Boolean activeProcess;
    private List<Filter> filters;
}
