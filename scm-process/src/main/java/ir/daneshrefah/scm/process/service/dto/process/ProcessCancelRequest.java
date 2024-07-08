package ir.daneshrefah.scm.process.service.dto.process;

import lombok.Data;

@Data
public class ProcessCancelRequest {
    private String processId;
    private String assigne; //TODO Remove this
}
