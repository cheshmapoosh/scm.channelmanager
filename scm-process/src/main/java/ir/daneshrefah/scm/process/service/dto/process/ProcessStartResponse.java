package ir.daneshrefah.scm.process.service.dto.process;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProcessStartResponse {
    private String processInstanceId;
    private String processDefinitionId;
    private LocalDateTime createdTime;
}
