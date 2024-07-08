package ir.daneshrefah.scm.process.service.dto.history;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class HistoryProcessResponse {
    private String id;
    private String rootProcessInstanceId;
    private String ProcessInstanceID;
    private String processName;
    private String state;
    private Long durationInMillis;
    private Long startTime;
    private Long endTime;
    private Long removalTime;
    private Map<String, Object> data;
}
