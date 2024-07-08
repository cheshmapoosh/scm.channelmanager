package ir.daneshrefah.scm.process.service.dto.history;

import ir.daneshrefah.scm.common.data.entity.person.GeneralRealPersonEntity;
import ir.daneshrefah.scm.process.model.process.ProcessInstanceInfo;
import ir.daneshrefah.scm.process.model.task.Assignment;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class HistoryTaskResponse {
    private String id;
    private List<Assignment> assignment;
    private String taskName;
    private Long startTime;
    private Long endTime;
    private boolean isDeleted;
    private String deleteReason;
    private String state;
    private ProcessInstanceInfo processInstance;
    private Map<String,Object> data;
}
