package ir.daneshrefah.scm.process.model.filter;

import ir.daneshrefah.scm.common.dto.PagedRequestData;
import lombok.Data;

import java.util.List;

@Data
public class ProcessFilter extends PagedRequestData {

    private Long startDate;
    private Long endDate;
    private String rootProcessInstanceId;
    private String nationalCode;//TODO remove this
    private String userVariable;
    private boolean activeProcess;
    private List<Filter> filters;
}
