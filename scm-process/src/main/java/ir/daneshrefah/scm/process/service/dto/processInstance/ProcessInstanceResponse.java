package ir.daneshrefah.scm.process.service.dto.processInstance;

import lombok.Data;

import java.util.Date;

@Data
public class ProcessInstanceResponse{
    private String id;
    private String key;
    private String username;
    private Date startTime;
    private boolean suspended;
}
