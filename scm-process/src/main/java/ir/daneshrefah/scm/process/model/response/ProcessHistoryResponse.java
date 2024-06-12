package ir.daneshrefah.scm.process.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@NoArgsConstructor
public class ProcessHistoryResponse {
    private String id;
    private String rootProcessInstanceId;
    private String ProcessInstanceID;
    private String startActivityId;
    private String processName;
    private String processPersianName;
    private String state;
    private String stateName;
    private Long durationInMillis;
    private Date startTime;
    private Long startTimeMills;
    private Date endTime;
    private Long endTimeMills;
    private Date removalTime;
    private Long removalTimeMills;
    private Map<String, Object> data;
}
